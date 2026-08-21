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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class JsyncDaemon {
  private static final long MAX_ACTIVE_TIME = 43200000L;
  
  private static final AtomicInteger lastId = new AtomicInteger();
  
  private static final SecureRandom random = new SecureRandom();
  
  private static final Map<Long, DaemonSession> activeSessions = new ConcurrentHashMap<>();
  
  public static void main(String[] args) {
    Logger.LOGGER.info(JsyncClient.getHeader("JsyncDaemon"));
    ServerSocket s = null;
    try {
      if (args.length < 1 || args.length > 2)
        throw new IllegalArgumentException("wrong number of command line arguments"); 
      int port = Integer.parseInt(args[0]);
      String ip = (args.length > 1) ? args[1] : null;
      s = new ServerSocket(port, 30, (ip == null) ? null : InetAddress.getByName(ip));
    } catch (Exception e) {
      Logger.LOGGER.info("command line: <port> (<ip>)");
      Logger.LOGGER.log(Level.SEVERE, "error during start-up", e);
      System.exit(99);
    } 
    Logger.LOGGER.info("JsyncDaemon started");
    while (true) {
      try {
        while (true) {
          Socket bound = s.accept();
          handleConnection(bound);
        } 
      } catch (Exception e) {
        Logger.LOGGER.log(Level.WARNING, "error during session init", e);
      } 
    } 
  }
  
  private static void handleConnection(Socket bound) throws IOException {
    InputStream in = bound.getInputStream();
    OutputStream out = bound.getOutputStream();
    DataInputStream dIn = new DataInputStream(in);
    DataOutputStream dOut = new DataOutputStream(out);
    String header = dIn.readUTF();
    if (header.equals("Init")) {
      String remoteParentDir = dIn.readUTF();
      boolean createDir = dIn.readBoolean();
      File dir = new File(remoteParentDir);
      if (createDir) {
        dir.mkdir();
        if (!dir.exists()) {
          String msg = "Target directory could not be created: " + remoteParentDir;
          sendError(bound, dOut, msg);
          return;
        } 
      } else if (!dir.exists()) {
        String msg = "Target directory does not exist: " + remoteParentDir;
        sendError(bound, dOut, msg);
        return;
      } 
      long sessionRunningNbr = lastId.incrementAndGet();
      long sessionSalt = random.nextInt() & 0xFFFFFFFFL;
      long sessionId = sessionRunningNbr << 32L | sessionSalt;
      Logger.LOGGER.info("creating session " + sessionId + " to dir " + dir + " from " + bound
          .getRemoteSocketAddress());
      DaemonSession session = new DaemonSession(sessionId, new FilePathAdapter(dir), () -> activeSessions.remove(Long.valueOf(sessionId)));
      cleanSessionMap();
      activeSessions.put(Long.valueOf(sessionId), session);
      session.connectChannel(1, bound);
      dOut.writeLong(sessionId);
    } else if (header.equals("Connect")) {
      long sessionId = dIn.readLong();
      DaemonSession session = activeSessions.get(Long.valueOf(sessionId));
      if (session == null) {
        Logger.LOGGER.warning("connection to missing session attempted: " + sessionId);
        dOut.write(1);
        bound.close();
        return;
      } 
      int channel = dIn.readByte();
      session.connectChannel(channel, bound);
      Logger.LOGGER.info("channel " + channel + " for " + sessionId + " connected. Now " + 
          Thread.activeCount() + " threads and " + activeSessions.size() + " sessions active.");
      dOut.write(0);
    } else {
      Logger.LOGGER.warning("invalid header received: " + header);
      bound.close();
    } 
  }
  
  private static void sendError(Socket bound, DataOutputStream dOut, String msg) throws IOException {
    Logger.LOGGER.warning(msg);
    dOut.writeInt(-1);
    dOut.writeUTF(msg);
    bound.close();
  }
  
  private static void cleanSessionMap() {
    Iterator<DaemonSession> iter = activeSessions.values().iterator();
    while (iter.hasNext()) {
      DaemonSession cur = iter.next();
      if (cur.getTimeSinceCreation() > 43200000L)
        iter.remove(); 
    } 
  }
}
