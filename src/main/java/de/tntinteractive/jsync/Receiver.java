/*
    Copyright (C) 2013-2017  Tobias Baum <tbaum at tntinteractive.de>

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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

/**
 * Empfängt das Diff vom {@link Sender} und baut daraufhin die Zieldatei auf.
 */
public class Receiver implements Runnable {

    private static final String TMP_SUFFIX = ".jstmp";

    private final DataInputStream input;
    private final FastConcurrentList<TargetFileInfo> filePaths;
    private final BlockingQueue<Integer> toResend;

    private boolean enumeratorDone;

    private int openResends;

    public Receiver(InputStream source, FastConcurrentList<TargetFileInfo> b, BlockingQueue<Integer> toResend) {
        this.input = new DataInputStream(source);
        this.filePaths = b;
        this.toResend = toResend;
    }

    @Override
    public void run() {
        try {
            int index = -2;
            FilePath tmpFile = null;
            MD4OutputStream tmpFileStream = null;
            RandomAccessInput templateFile = null;

            while (!Thread.interrupted()) {
                final int command = this.input.read();
                if (command < 0) {
                    break;
                }
                if (command == ReceiverCommand.FILE_START.getCode()) {
                    //Anfang einer neuen Datei => Tempdatei erzeugen
                    index = this.input.readInt();
                    tmpFile = this.createTempFileFor(index);
                    tmpFileStream = new MD4OutputStream(tmpFile.openOutputStream());
                } else if (command == ReceiverCommand.RAW_DATA.getCode()) {
                    //Rohdaten => in Tempdatei schreiben
                    final int length = this.input.readInt();
                    StreamHelper.copy(this.input, tmpFileStream, length);
                } else if (command == ReceiverCommand.COPY_BLOCK.getCode()) {
                    //Block aus Quelldatei kopieren
                    final long offset = this.input.readLong();
                    final short length = this.input.readShort();
                    if (templateFile == null) {
                        templateFile = this.filePaths.get(index).getFilePath().openRandomAccessInput();
                    }
                    templateFile.copyTo(tmpFileStream, offset, length);
                } else if (command == ReceiverCommand.FILE_END.getCode()) {
                    if (templateFile != null) {
                        templateFile.close();
                        templateFile = null;
                    }

                    //Ende der Datei => Prüfsumme prüfen
                    final byte[] expectedDigest = new byte[MD4.DIGEST_LENGTH];
                    this.input.readFully(expectedDigest);

                    if (Arrays.equals(expectedDigest, tmpFileStream.getDigest())) {
                        //Prüfsumme OK => echte Datei mit Tempdatei überschreiben
                        tmpFileStream.close();
                        this.renameToRealName(index, tmpFile);
                        if (this.enumeratorDone) {
                            this.openResends--;
                            assert this.openResends >= 0;
                            if (this.openResends == 0) {
                                break;
                            }
                        }
                    } else {
                        //Prüfsumme nicht OK => Datei in die Resend-Queue stecken
                        this.toResend.add(index);
                        if (!this.enumeratorDone) {
                            this.openResends++;
                        }
                    }
                } else if (command == ReceiverCommand.ENUMERATOR_DONE.getCode()) {
                    this.enumeratorDone = true;
                    if (this.openResends == 0) {
                        break;
                    }
                } else {
                    throw new IOException("unknown command " + command);
                }
            }

            //dem Generator sagen, dass nichts mehr kommt
            this.toResend.add(-1);
        } catch (final Exception e) {
            Logger.LOGGER.log(Level.SEVERE, "exception in receiver", e);
            try {
                this.input.close();
            } catch (final IOException e1) {
                Logger.LOGGER.log(Level.WARNING, "exception while closing", e1);
            }
        }
    }

    private FilePath createTempFileFor(int index) {
        final FilePath orig = this.filePaths.get(index).getFilePath();
        return orig.getParent().getChild(orig.getName() + TMP_SUFFIX);
    }

    private void renameToRealName(int index, FilePath tmpFile) throws IOException {
        tmpFile.setLastChange(this.filePaths.get(index).getSourceChangeTime());
        tmpFile.renameTo(this.filePaths.get(index).getFilePath().getName());
    }

}
