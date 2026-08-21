package de.tntinteractive.jsync;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class ExceptionBuffer {
  private final CopyOnWriteArrayList<Exception> buffer = new CopyOnWriteArrayList<>();
  
  public void addThrowable(Exception t) {
    Logger.LOGGER.log(Level.SEVERE, "fatal error occurred", t);
    this.buffer.add(t);
  }
  
  public void doHandling() throws Exception {
    if (!this.buffer.isEmpty())
      throw (Exception)this.buffer.get(0); 
  }
}
