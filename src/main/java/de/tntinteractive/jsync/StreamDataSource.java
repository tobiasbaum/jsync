package de.tntinteractive.jsync;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class StreamDataSource implements DataSource {
  public CommandReader readCommand() throws IOException, ExecutionException, InterruptedException, TimeoutException {
    DataInputStream d = new DataInputStream(getStream());
    int code = d.read();
    if (code < 0)
      return null; 
    return new CommandReader(code, d);
  }
  
  protected abstract InputStream getStream() throws IOException, ExecutionException, InterruptedException, TimeoutException;
}
