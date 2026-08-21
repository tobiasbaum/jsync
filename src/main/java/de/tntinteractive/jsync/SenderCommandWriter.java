package de.tntinteractive.jsync;

import java.util.logging.Level;

class SenderCommandWriter {
  private final DataTarget output;
  
  public SenderCommandWriter(DataTarget output) {
    this.output = output;
  }
  
  void writeFileStart(int index, int strongHashSize, int blockSize) throws Exception {
    assert strongHashSize <= 127;
    assert blockSize <= 32767;
    this.output.writeCommand(CommandBuilder.of(SenderCommand.FILE_START.getCode())
        .writeInt(index)
        .writeByte(strongHashSize)
        .writeShort((short)blockSize));
  }
  
  void writeHashes(int rollingHash, byte[] strongHash) throws Exception {
    this.output.writeCommand(CommandBuilder.of(SenderCommand.HASH.getCode())
        .writeInt(rollingHash)
        .write(strongHash));
  }
  
  void writeFileEnd() throws Exception {
    this.output.writeCommand(CommandBuilder.of(SenderCommand.FILE_END.getCode()));
  }
  
  void writeEnumeratorDone() throws Exception {
    Logger.LOGGER.log(Level.FINE, "write enumerator done");
    this.output.writeCommand(CommandBuilder.of(SenderCommand.ENUMERATOR_DONE.getCode()));
  }
  
  void writeEverythingOk() throws Exception {
    Logger.LOGGER.log(Level.FINE, "write everything ok");
    this.output.writeCommand(CommandBuilder.of(SenderCommand.EVERYTHING_OK.getCode()));
  }
}
