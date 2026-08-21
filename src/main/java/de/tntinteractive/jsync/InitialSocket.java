/*
    Copyright (C) 2013-2026  Tobias Baum <tbaum at tntinteractive.de>

    This file is a part of jsync.

    jsync is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jsync is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jsync.  If not, see <http://www.gnu.org/licenses/>.
 */
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
