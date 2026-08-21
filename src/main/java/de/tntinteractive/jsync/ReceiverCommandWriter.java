package de.tntinteractive.jsync;

import java.io.InputStream;

public class ReceiverCommandWriter {
  private final DataTarget output;
  
  public ReceiverCommandWriter(DataTarget target) {
    this.output = target;
  }
  
  public void writeFileStart(int index) throws Exception {
    this.output.writeCommand(CommandBuilder.of(ReceiverCommand.FILE_START.getCode())
        .writeInt(index));
  }
  
  public void writeFileEnd(byte[] hash) throws Exception {
    this.output.writeCommand(CommandBuilder.of(ReceiverCommand.FILE_END.getCode())
        .write(hash));
  }
  
  public void writeRawData(int length, InputStream data) throws Exception {
    this.output.writeCommand(ReceiverCommand.RAW_DATA.getCode(), length, data);
  }
  
  public void writeCopyBlock(long startOffset, short length) throws Exception {
    this.output.writeCommand(CommandBuilder.of(ReceiverCommand.COPY_BLOCK.getCode())
        .writeLong(startOffset)
        .writeShort(length));
  }
  
  public void writeEnumeratorDone() throws Exception {
    this.output.writeCommand(CommandBuilder.of(ReceiverCommand.ENUMERATOR_DONE.getCode()));
  }
}
