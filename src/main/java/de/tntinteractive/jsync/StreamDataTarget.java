package de.tntinteractive.jsync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class StreamDataTarget implements DataTarget {
  public synchronized void writeCommand(CommandBuilder code) throws IOException, ExecutionException, InterruptedException, TimeoutException {
    getStream().write(code.toBytes());
  }
  
  public synchronized void writeCommand(byte code, int length, InputStream data) throws IOException, ExecutionException, InterruptedException, TimeoutException {
    OutputStream stream = getStream();
    stream.write(code);
    stream.write(length >>> 24 & 0xFF);
    stream.write(length >>> 16 & 0xFF);
    stream.write(length >>> 8 & 0xFF);
    stream.write(length >>> 0 & 0xFF);
    StreamHelper.copy(data, stream, length);
  }
  
  public synchronized void writeNop() throws IOException, ExecutionException, InterruptedException, TimeoutException {
    getStream().write(0);
  }
  
  protected abstract OutputStream getStream() throws IOException, ExecutionException, InterruptedException, TimeoutException;
}
