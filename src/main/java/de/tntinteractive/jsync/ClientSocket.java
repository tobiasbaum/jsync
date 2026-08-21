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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class ClientSocket {
  public static final String CONNECT_HEADER = "Connect";
  
  private final String remoteHost;
  
  private final int remotePort;
  
  private final long sessionId;
  
  private final int channel;
  
  private Socket socket;
  
  private final DataSource source;
  
  private final DataTarget target;
  
  private final Thread nopThread;
  
  private ClientSocket(String remoteHost, int remotePort, long sessionId, int channel, Socket socket) throws IOException {
    this.remoteHost = remoteHost;
    this.remotePort = remotePort;
    this.sessionId = sessionId;
    this.channel = channel;
    this.socket = socket;
    this.target = new StreamDataTarget() {
        protected OutputStream getStream() throws IOException {
          if (ClientSocket.this.socket == null)
            ClientSocket.this.connect(); 
          return ClientSocket.this.socket.getOutputStream();
        }
      };
    this.source = new StreamDataSource() {
        protected InputStream getStream() throws IOException, ExecutionException, InterruptedException, TimeoutException {
          if (ClientSocket.this.socket == null)
            ClientSocket.this.connect(); 
          return ClientSocket.this.socket.getInputStream();
        }
      };
    this.nopThread = new Thread(new Runnable() {
          public void run() {
            try {
              while (!Thread.interrupted()) {
                Thread.sleep(5000L);
                ClientSocket.this.getDataTarget().writeNop();
              } 
            } catch (InterruptedException e) {
              Logger.LOGGER.log(Level.FINEST, "interrupted exception for nop", e);
            } catch (Exception e) {
              Logger.LOGGER.log(Level.INFO, "exception for nop", e);
            } 
            Logger.LOGGER.log(Level.FINE, "nop thread ended");
          }
        }, "nop-thread");
    this.nopThread.setDaemon(true);
    this.nopThread.start();
  }
  
  public static ClientSocket forSecondChannel(String remoteHost, int remotePort, long sessionId) throws IOException {
    ClientSocket ret = new ClientSocket(remoteHost, remotePort, sessionId, 2, null);
    ret.connect();
    return ret;
  }
  
  public static ClientSocket fromInitSocket(InitialSocket s) throws IOException {
    return new ClientSocket(s.getRemoteHost(), s.getRemotePort(), s.getSessionId(), 1, s.getSocket());
  }
  
  private void connect() throws IOException {
    this.socket = InitialSocket.connectRaw(this.remoteHost, this.remotePort);
    initChannel(this.socket.getOutputStream(), this.socket.getInputStream(), this.sessionId, this.channel);
  }
  
  private static void initChannel(OutputStream ch2out, InputStream ch2in, long sessionId, int channel) throws IOException {
    DataOutputStream out = new DataOutputStream(ch2out);
    out.writeUTF("Connect");
    out.writeLong(sessionId);
    out.writeByte(channel);
    int resp = ch2in.read();
    if (resp != 0)
      throw new IOException("could not init channel " + channel + " in session " + sessionId + ": " + resp); 
  }
  
  public DataTarget getDataTarget() throws IOException {
    return this.target;
  }
  
  public DataSource getDataSource() throws IOException {
    return this.source;
  }
  
  public void close() throws IOException {
    this.nopThread.interrupt();
    try {
      this.nopThread.join(200000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } 
    this.socket.close();
  }
}
