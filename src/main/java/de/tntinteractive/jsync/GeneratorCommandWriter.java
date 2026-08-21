package de.tntinteractive.jsync;

class GeneratorCommandWriter {
  private final DataTarget output;
  
  public GeneratorCommandWriter(DataTarget output) {
    this.output = output;
  }
  
  void writeStepDown(String name) throws Exception {
    this.output.writeCommand(CommandBuilder.of(GeneratorCommand.STEP_DOWN.getCode())
        .writeUTF(name));
  }
  
  void writeFile(String name, long size, long lastChange) throws Exception {
    this.output.writeCommand(CommandBuilder.of(GeneratorCommand.FILE.getCode())
        .writeUTF(name)
        .writeLong(size)
        .writeLong(lastChange));
  }
  
  void writeStepUp() throws Exception {
    this.output.writeCommand(CommandBuilder.of(GeneratorCommand.STEP_UP.getCode()));
  }
  
  public void writeEnd() throws Exception {
    this.output.writeCommand(CommandBuilder.of(GeneratorCommand.END.getCode()));
  }
}
