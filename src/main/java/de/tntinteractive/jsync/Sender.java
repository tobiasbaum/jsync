package de.tntinteractive.jsync;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Sender implements Runnable {
  private static final int RAW_DATA_BUFFER_LIMIT = 8096;
  
  private final DataSource source;
  
  private final FastConcurrentList<FilePath> filePaths;
  
  private final ReceiverCommandWriter writer;
  
  private final ExceptionBuffer exc;
  
  private int count;
  
  private int countFully;
  
  public Sender(DataSource source, FastConcurrentList<FilePath> filePaths, DataTarget target, ExceptionBuffer exc) {
    this.source = source;
    this.filePaths = filePaths;
    this.writer = new ReceiverCommandWriter(target);
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
    
    public void writeCopyCommand(ReceiverCommandWriter writer, int blockSize) throws Exception {
      writer.writeCopyBlock(this.blockNumber * blockSize, (short)blockSize);
    }
  }
  
  public void run() {
    try {
      int index = -1;
      int strongHashSize = -1;
      int blockSize = -1;
      Map<Integer, List<BlockInfo>> hashes = new HashMap<>();
      int blockNumber = -1;
      Logger.LOGGER.log(Level.FINE, "sender started");
      while (!Thread.interrupted()) {
        CommandReader command = this.source.readCommand();
        Logger.LOGGER.log(Level.FINE, "sender got command " + command);
        if (command == null)
          throw new IOException("Error while copying! Check daemon log for details."); 
        if (command.code == SenderCommand.FILE_START.getCode()) {
          index = command.input.readInt();
          strongHashSize = command.input.readByte();
          blockSize = command.input.readShort();
          blockNumber = 0;
          hashes.clear();
          continue;
        } 
        if (command.code == SenderCommand.HASH.getCode()) {
          Integer rollingHash = Integer.valueOf(command.input.readInt());
          List<BlockInfo> blocksForHash = hashes.get(rollingHash);
          if (blocksForHash == null) {
            blocksForHash = new ArrayList<>(1);
            hashes.put(rollingHash, blocksForHash);
          } 
          byte[] strongHash = new byte[strongHashSize];
          command.input.readFully(strongHash);
          blocksForHash.add(new BlockInfo(strongHash, blockNumber));
          blockNumber++;
          continue;
        } 
        if (command.code == SenderCommand.FILE_END.getCode()) {
          if (hashes.isEmpty()) {
            copyFileFully(index);
            this.countFully++;
          } else {
            copyFileUsingDiff(index, hashes, blockSize, strongHashSize);
          } 
          this.count++;
          continue;
        } 
        if (command.code == SenderCommand.ENUMERATOR_DONE.getCode()) {
          this.writer.writeEnumeratorDone();
          continue;
        } 
        if (command.code == SenderCommand.EVERYTHING_OK.getCode())
          break; 
        throw new IOException("unknown command " + command);
      } 
      System.out.println("Had to send " + this.count + " files, " + this.countFully + " of these fully.");
    } catch (Exception e) {
      this.exc.addThrowable(e);
    } 
  }
  
  private void copyFileUsingDiff(int index, Map<Integer, List<BlockInfo>> hashes, int blockSize, int strongHashSize) throws Exception {
    FilePath file = this.filePaths.get(index);
    InputStream fileStream = file.openInputStream();
    try {
      MD4InputStream md4stream = new MD4InputStream(fileStream);
      BufferedInputStream bufferedStream = new BufferedInputStream(md4stream);
      this.writer.writeFileStart(index);
      Checksum32 rollingChecksum = new Checksum32();
      ByteArrayOutputStream rawDataBuffer = new ByteArrayOutputStream();
      byte[] block = new byte[blockSize];
      StreamHelper.readFully(bufferedStream, block);
      rollingChecksum.check(block, 0, block.length);
      while (true) {
        int currentChecksum = rollingChecksum.getValue();
        List<BlockInfo> blocksWithChecksum = hashes.get(Integer.valueOf(currentChecksum));
        if (blocksWithChecksum != null) {
          rollingChecksum.copyBlock(block);
          byte[] currentMD4 = MD4.determineFor(block, strongHashSize);
          Iterator<BlockInfo> iterator = blocksWithChecksum.iterator();
          while (true) {
            if (iterator.hasNext()) {
              BlockInfo b = iterator.next();
              if (b.matches(currentMD4)) {
                if (rawDataBuffer.size() > 0)
                  flushRawData(rawDataBuffer); 
                b.writeCopyCommand(this.writer, blockSize);
                int readCount = StreamHelper.readFully(bufferedStream, block);
                if (readCount < block.length) {
                  rawDataBuffer.write(block, 0, readCount);
                } else {
                  rollingChecksum.check(block, 0, block.length);
                  continue;
                } 
              } else {
                continue;
              } 
            } else {
              break;
            } 
            if (rawDataBuffer.size() > 0)
              flushRawData(rawDataBuffer); 
            this.writer.writeFileEnd(md4stream.getDigest());
            fileStream.close();
          } 
        } 
        int nextByte = bufferedStream.read();
        if (nextByte < 0) {
          rollingChecksum.copyBlock(block);
          rawDataBuffer.write(block);
          break;
        } 
        rawDataBuffer.write(rollingChecksum.roll((byte)nextByte));
        if (rawDataBuffer.size() > 8096)
          flushRawData(rawDataBuffer); 
      } 
      if (rawDataBuffer.size() > 0)
        flushRawData(rawDataBuffer); 
      this.writer.writeFileEnd(md4stream.getDigest());
    } finally {
      fileStream.close();
    } 
  }
  
  private void flushRawData(ByteArrayOutputStream rawDataBuffer) throws Exception {
    byte[] data = rawDataBuffer.toByteArray();
    this.writer.writeRawData(data.length, new ByteArrayInputStream(data));
    rawDataBuffer.reset();
  }
  
  private void copyFileFully(int index) throws Exception {
    FilePath file = this.filePaths.get(index);
    InputStream fileStream = file.openInputStream();
    try {
      MD4InputStream md4stream = new MD4InputStream(fileStream);
      this.writer.writeFileStart(index);
      long remainingBytes = file.getSize();
      while (remainingBytes > 0L) {
        long inThisChunk = Math.min(remainingBytes, 2147483647L);
        this.writer.writeRawData((int)inThisChunk, md4stream);
        remainingBytes -= inThisChunk;
      } 
      this.writer.writeFileEnd(md4stream.getDigest());
    } finally {
      fileStream.close();
    } 
  }
}
