package de.tntinteractive.jsync;

import java.io.DataInputStream;

public class CommandReader {
  public int code;
  
  public DataInputStream input;
  
  public CommandReader(int code, DataInputStream d) {
    this.code = code;
    this.input = d;
  }
  
  public String toString() {
    return Integer.toString(this.code);
  }
}
