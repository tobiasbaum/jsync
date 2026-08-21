package de.tntinteractive.jsync;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface DataTarget {
  void writeCommand(CommandBuilder paramCommandBuilder) throws IOException, ExecutionException, InterruptedException, TimeoutException;
  
  void writeCommand(byte paramByte, int paramInt, InputStream paramInputStream) throws IOException, ExecutionException, InterruptedException, TimeoutException;
  
  void writeNop() throws IOException, ExecutionException, InterruptedException, TimeoutException;
}
