package de.tntinteractive.jsync;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

public class Receiver implements Runnable {
  private static final String TMP_SUFFIX = ".jstmp";
  
  private final DataSource input;
  
  private final FastConcurrentList<TargetFileInfo> filePaths;
  
  private final BlockingQueue<Integer> toResend;
  
  private final Runnable closeCallback;
  
  private boolean enumeratorDone;
  
  private int openResends;
  
  public Receiver(DataSource source, FastConcurrentList<TargetFileInfo> b, BlockingQueue<Integer> toResend, Runnable closeCallback) {
    this.input = source;
    this.filePaths = b;
    this.toResend = toResend;
    this.closeCallback = closeCallback;
  }
  
  public void run() {
    int index = -2;
    try {
      FilePath tmpFile = null;
      MD4OutputStream tmpFileStream = null;
      RandomAccessInput templateFile = null;
      while (!Thread.interrupted()) {
        CommandReader command = this.input.readCommand();
        if (command == null)
          break; 
        Logger.LOGGER.log(Level.FINE, "receiver got command " + command.code);
        if (command.code == ReceiverCommand.NOP.getCode())
          continue; 
        if (command.code == ReceiverCommand.FILE_START.getCode()) {
          index = command.input.readInt();
          tmpFile = createTempFileFor(index);
          tmpFileStream = new MD4OutputStream(tmpFile.openOutputStream());
          continue;
        } 
        if (command.code == ReceiverCommand.RAW_DATA.getCode()) {
          int length = command.input.readInt();
          StreamHelper.copy(command.input, tmpFileStream, length);
          continue;
        } 
        if (command.code == ReceiverCommand.COPY_BLOCK.getCode()) {
          long offset = command.input.readLong();
          short length = command.input.readShort();
          if (templateFile == null)
            templateFile = ((TargetFileInfo)this.filePaths.get(index)).getFilePath().openRandomAccessInput(); 
          templateFile.copyTo(tmpFileStream, offset, length);
          continue;
        } 
        if (command.code == ReceiverCommand.FILE_END.getCode()) {
          if (templateFile != null) {
            templateFile.close();
            templateFile = null;
          } 
          byte[] expectedDigest = new byte[16];
          command.input.readFully(expectedDigest);
          byte[] actualDigest = tmpFileStream.getDigest();
          tmpFileStream.close();
          if (Arrays.equals(expectedDigest, actualDigest)) {
            renameToRealName(index, tmpFile);
            if (this.enumeratorDone) {
              this.openResends--;
              assert this.openResends >= 0;
              if (this.openResends == 0)
                break; 
            } 
            continue;
          } 
          Logger.LOGGER.log(Level.INFO, "checksum mismatch, resending " + index);
          this.toResend.add(Integer.valueOf(index));
          if (!this.enumeratorDone)
            this.openResends++; 
          continue;
        } 
        if (command.code == ReceiverCommand.ENUMERATOR_DONE.getCode()) {
          this.enumeratorDone = true;
          if (this.openResends == 0)
            break; 
          continue;
        } 
        throw new IOException("unknown command " + command);
      } 
      this.toResend.add(Integer.valueOf(-1));
    } catch (Exception e) {
      Logger.LOGGER.log(Level.SEVERE, "exception in receiver", e);
      this.toResend.add(Integer.valueOf(-1));
      this.closeCallback.run();
    } 
  }
  
  private FilePath createTempFileFor(int index) {
    FilePath orig = ((TargetFileInfo)this.filePaths.get(index)).getFilePath();
    return orig.getParent().getChild(orig.getName() + ".jstmp");
  }
  
  private void renameToRealName(int index, FilePath tmpFile) throws IOException {
    tmpFile.setLastChange(((TargetFileInfo)this.filePaths.get(index)).getSourceChangeTime());
    tmpFile.renameTo(((TargetFileInfo)this.filePaths.get(index)).getFilePath().getName());
  }
}
