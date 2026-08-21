package de.tntinteractive.jsync;

import java.io.Closeable;
import java.io.IOException;

public class StableConnection implements Closeable {
  private ClientSocket ch1;
  
  private ClientSocket ch2;
  
  public StableConnection(String remoteHost, int remotePort, String remoteParentDirectory, boolean createDir) throws IOException {
    InitialSocket is = InitialSocket.initiate(remoteHost, remotePort, remoteParentDirectory, createDir);
    this.ch1 = ClientSocket.fromInitSocket(is);
    this.ch2 = ClientSocket.forSecondChannel(remoteHost, remotePort, is.getSessionId());
  }
  
  public DataTarget getEnumeratorOutput() throws IOException {
    return this.ch1.getDataTarget();
  }
  
  public DataSource getSenderInput() throws IOException {
    return this.ch1.getDataSource();
  }
  
  public DataTarget getSenderOutput() throws IOException {
    return this.ch2.getDataTarget();
  }
  
  public void close() throws IOException {
    if (this.ch1 != null)
      this.ch1.close(); 
    if (this.ch2 != null)
      this.ch2.close(); 
  }
}
