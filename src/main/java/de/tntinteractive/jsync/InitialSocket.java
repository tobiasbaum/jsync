package de.tntinteractive.jsync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class InitialSocket {
  public static final String INIT_HEADER = "Init";
  
  private final String remoteHost;
  
  private final int remotePort;
  
  private final long sessionId;
  
  private final Socket socket;
  
  private InitialSocket(String remoteHost, int remotePort, Socket socket, long sessionId) {
    this.remoteHost = remoteHost;
    this.remotePort = remotePort;
    this.socket = socket;
    this.sessionId = sessionId;
  }
  
  static Socket connectRaw(String remoteHost, int remotePort) throws IOException {
    Socket s = new Socket(remoteHost, remotePort);
    s.setKeepAlive(true);
    return s;
  }
  
  public static InitialSocket initiate(String remoteHost, int remotePort, String remoteParentDirectory, boolean createDir) throws IOException {
    Socket s = connectRaw(remoteHost, remotePort);
    long session = initiateSession(s.getInputStream(), s.getOutputStream(), remoteParentDirectory, createDir);
    return new InitialSocket(remoteHost, remotePort, s, session);
  }
  
  private static long initiateSession(InputStream ch1in, OutputStream ch1out, String remoteParentDirectory, boolean createDir) throws IOException {
    DataOutputStream out = new DataOutputStream(ch1out);
    out.writeUTF("Init");
    out.writeUTF(remoteParentDirectory);
    out.writeBoolean(createDir);
    DataInputStream in = new DataInputStream(ch1in);
    long sessionId = in.readLong();
    if (sessionId < 0L)
      throw new IOException("Error while initiating session: " + in.readUTF()); 
    return sessionId;
  }
  
  Socket getSocket() {
    return this.socket;
  }
  
  public long getSessionId() {
    return this.sessionId;
  }
  
  public String getRemoteHost() {
    return this.remoteHost;
  }
  
  public int getRemotePort() {
    return this.remotePort;
  }
}
