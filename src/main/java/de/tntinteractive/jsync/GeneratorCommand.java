package de.tntinteractive.jsync;

public enum GeneratorCommand {
  NOP(0),
  STEP_DOWN(1),
  FILE(2),
  STEP_UP(3),
  END(4);
  
  private final byte code;
  
  GeneratorCommand(int code) {
    this.code = (byte)code;
  }
  
  public byte getCode() {
    return this.code;
  }
}
