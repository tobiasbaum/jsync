/*
    Copyright (C) 2013  Tobias Baum <tbaum at tntinteractive.de>

    This file is a part of jsync.

    jsync is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jsync is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jsync.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tntinteractive.jsync;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bekommt vom {@link Generator} die Befehle zum Verschicken von Dateien und schickt das passende Diff
 * an den {@link Receiver}.
 */
public class Sender implements Runnable {

    private static final int RAW_DATA_BUFFER_LIMIT = 8096;

    private final DataInputStream source;
    private final FastConcurrentList<FilePath> filePaths;
    private final ReceiverCommandWriter writer;
    private final ExceptionBuffer exc;

    private int count;

    public Sender(InputStream source, FastConcurrentList<FilePath> filePaths, OutputStream target, ExceptionBuffer exc) {
        this.source = new DataInputStream(source);
        this.filePaths = filePaths;
        this.writer = new ReceiverCommandWriter(new DataOutputStream(target));
        this.exc = exc;
    }

    private static class BlockInfo {

        private final byte[] strongHash;
        private final int blockNumber;

        public BlockInfo(byte[] strongHash, int blockNumber) {
            this.strongHash = strongHash;
            this.blockNumber = blockNumber;
        }

        public boolean matches(byte[] currentMD4) {
            return Arrays.equals(this.strongHash, currentMD4);
        }

        public void writeCopyCommand(ReceiverCommandWriter writer, int blockSize) throws IOException {
            writer.writeCopyBlock(this.blockNumber * ((long) blockSize), (short) blockSize);
        }

    }

    @Override
    public void run() {
        try {
            boolean okReceived = false;
            int index = -1;
            int strongHashSize = -1;
            int blockSize = -1;
            final Map<Integer, List<BlockInfo>> hashes = new HashMap<Integer, List<BlockInfo>>();
            int blockNumber = -1;

            while (!Thread.interrupted()) {
                final int command = this.source.read();
                if (command < 0) {
                    if (!okReceived) {
                        throw new IOException("Error while copying! Check daemon log for details.");
                    }
                    break;
                }
                if (command == SenderCommand.FILE_START.getCode()) {
                    index = this.source.readInt();
                    strongHashSize = this.source.readByte();
                    blockSize = this.source.readShort();
                    blockNumber = 0;
                    hashes.clear();
                } else if (command == SenderCommand.HASH.getCode()) {
                    final Integer rollingHash = this.source.readInt();
                    List<BlockInfo> blocksForHash = hashes.get(rollingHash);
                    if (blocksForHash == null) {
                        blocksForHash = new ArrayList<BlockInfo>(1);
                        hashes.put(rollingHash, blocksForHash);
                    }
                    final byte[] strongHash = new byte[strongHashSize];
                    this.source.readFully(strongHash);
                    blocksForHash.add(new BlockInfo(strongHash, blockNumber));
                    blockNumber++;
                } else if (command == SenderCommand.FILE_END.getCode()) {
                    if (hashes.isEmpty()) {
                        this.copyFileFully(index);
                    } else {
                        this.copyFileUsingDiff(index, hashes, blockSize, strongHashSize);
                    }
                    this.count++;
                } else if (command == SenderCommand.ENUMERATOR_DONE.getCode()) {
                    this.writer.writeEnumeratorDone();
                } else if (command == SenderCommand.EVERYTHING_OK.getCode()) {
                    okReceived = true;
                } else {
                    throw new IOException("unknown command " + command);
                }
            }

            System.out.println("Had to send " + this.count + " files");
        } catch (final IOException e) {
            this.exc.addThrowable(e);
        } finally {
            this.writer.close();
        }
    }

    private void copyFileUsingDiff(int index, Map<Integer, List<BlockInfo>> hashes, int blockSize,
            int strongHashSize) throws IOException {
        final FilePath file = this.filePaths.get(index);
        final InputStream fileStream = file.openInputStream();
        try {
            final MD4InputStream md4stream = new MD4InputStream(fileStream);
            final BufferedInputStream bufferedStream = new BufferedInputStream(md4stream);
            this.writer.writeFileStart(index);

            final Checksum32 rollingChecksum = new Checksum32();
            final ByteArrayOutputStream rawDataBuffer = new ByteArrayOutputStream();
            final byte[] block = new byte[blockSize];
            StreamHelper.readFully(bufferedStream, block);
            rollingChecksum.check(block, 0, block.length);

            outerLoop: while (true) {
                final int currentChecksum = rollingChecksum.getValue();
                final List<BlockInfo> blocksWithChecksum = hashes.get(currentChecksum);
                if (blocksWithChecksum != null) {
                    rollingChecksum.copyBlock(block);
                    final byte[] currentMD4 = MD4.determineFor(block, strongHashSize);
                    for (final BlockInfo b : blocksWithChecksum) {
                        if (b.matches(currentMD4)) {
                            if (rawDataBuffer.size() > 0) {
                                this.flushRawData(rawDataBuffer);
                            }
                            b.writeCopyCommand(this.writer, blockSize);
                            final int readCount = StreamHelper.readFully(bufferedStream, block);
                            if (readCount < block.length) {
                                //EOF
                                rawDataBuffer.write(block, 0, readCount);
                                break outerLoop;
                            } else {
                                rollingChecksum.check(block, 0, block.length);
                                continue outerLoop;
                            }
                        }
                    }
                }

                //wenn er hier hinkommt, dann hat er kein Match gefunden
                final int nextByte = bufferedStream.read();
                if (nextByte < 0) {
                    //EOF
                    rollingChecksum.copyBlock(block);
                    rawDataBuffer.write(block);
                    break outerLoop;
                }
                rawDataBuffer.write(rollingChecksum.roll((byte) nextByte));
                if (rawDataBuffer.size() > RAW_DATA_BUFFER_LIMIT) {
                    this.flushRawData(rawDataBuffer);
                }
            }

            if (rawDataBuffer.size() > 0) {
                this.flushRawData(rawDataBuffer);
            }

            this.writer.writeFileEnd(md4stream.getDigest());
        } finally {
            fileStream.close();
        }
    }

    private void flushRawData(ByteArrayOutputStream rawDataBuffer) throws IOException {
        final byte[] data = rawDataBuffer.toByteArray();
        this.writer.writeRawData(data.length, new ByteArrayInputStream(data));
        rawDataBuffer.reset();
    }

    private void copyFileFully(int index) throws IOException {
        final FilePath file = this.filePaths.get(index);
        final InputStream fileStream = file.openInputStream();
        try {
            final MD4InputStream md4stream = new MD4InputStream(fileStream);
            this.writer.writeFileStart(index);
            long remainingBytes = file.getSize();
            while (remainingBytes > 0) {
                final long inThisChunk = Math.min(remainingBytes, Integer.MAX_VALUE);
                this.writer.writeRawData((int) inThisChunk, md4stream);
                remainingBytes -= inThisChunk;
            }
            this.writer.writeFileEnd(md4stream.getDigest());
        } finally {
            fileStream.close();
        }
    }

}
