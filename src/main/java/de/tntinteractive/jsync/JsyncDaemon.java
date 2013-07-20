/*
    Copyright (C) 2013  Tobias Baum <tbaum at tntinteractive.de>

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

/**
 * Hauptklasse für den Daemon-Teil.
 * Der Daemon muss auf dem Zielsystem gestartet werden und nimmt dann Anfragen entgegen.
 */
public class JsyncDaemon {

    private static final long MAX_WAIT_TIME_FOR_SECOND_CHANNEL = 30 *1000;

    private static int lastId;
    private static final Map<Integer, DaemonSession> sessionsWithMissingChannel = new HashMap<Integer, DaemonSession>();

    public static void main(String[] args) {
        Logger.LOGGER.info(JsyncClient.getHeader("JsyncDaemon"));
        ServerSocket s = null;
        try {
            if (args.length < 1 || args.length > 2) {
                throw new IllegalArgumentException("wrong number of command line arguments");
            }
            final int port = Integer.parseInt(args[0]);
            final String ip = args.length > 1 ? args[1] : null;
            s = new ServerSocket(port, 30, ip == null ? null : InetAddress.getByName(ip));
        } catch (final Exception e) {
            Logger.LOGGER.info("command line: <port> (<ip>)");
            Logger.LOGGER.log(Level.SEVERE, "error during start-up", e);
            System.exit(99);
        }

        Logger.LOGGER.info("JsyncDaemon started");

        Socket bound;
        while (true) {
            try {
                bound = s.accept();
                handleConnection(bound);
            } catch (final Exception e) {
                Logger.LOGGER.log(Level.WARNING, "error during session init", e);
            }
        }
    }

    private static void handleConnection(Socket bound) throws IOException {
        final InputStream in = bound.getInputStream();
        final OutputStream out = bound.getOutputStream();
        final DataInputStream dIn = new DataInputStream(in);
        final DataOutputStream dOut = new DataOutputStream(out);

        final String header = dIn.readUTF();
        if (header.equals(JsyncClient.FIRST_CHANNEL_HEADER)) {
            final String remoteParentDir = dIn.readUTF();
            final boolean createDir = dIn.readBoolean();
            final File dir = new File(remoteParentDir);
            if (createDir) {
                dir.mkdir();
                if (!dir.exists()) {
                    final String msg = "Target directory could not be created: " + remoteParentDir;
                    sendError(bound, dOut, msg);
                    return;
                }
            } else {
                if (!dir.exists()) {
                    final String msg = "Target directory does not exist: " + remoteParentDir;
                    sendError(bound, dOut, msg);
                    return;
                }
            }

            final int sessionId = lastId++;
            Logger.LOGGER.info("creating session " + sessionId + " to dir "
                    + dir + " from " + bound.getRemoteSocketAddress());

            final DaemonSession session = new DaemonSession(sessionId, new FilePathAdapter(dir));
            cleanSessionMap();
            sessionsWithMissingChannel.put(sessionId, session);

            session.addFirstChannel(bound, in, out);

            dOut.writeInt(sessionId);
        } else if (header.equals(JsyncClient.SECOND_CHANNEL_HEADER)) {
            final int sessionId = dIn.readInt();
            final DaemonSession session = sessionsWithMissingChannel.get(sessionId);
            if (session == null) {
                Logger.LOGGER.warning("connection to missing session attempted: " + sessionId);
                bound.close();
                return;
            }
            sessionsWithMissingChannel.remove(sessionId);
            session.addSecondChannel(bound, in, out);
        } else {
            Logger.LOGGER.warning("invalid header received: " + header);
            bound.close();
        }
    }

    private static void sendError(Socket bound, final DataOutputStream dOut, final String msg) throws IOException {
        Logger.LOGGER.warning(msg);
        dOut.writeInt(-1);
        dOut.writeUTF(msg);
        bound.close();
    }

    private static void cleanSessionMap() {
        final Iterator<DaemonSession> iter = sessionsWithMissingChannel.values().iterator();
        while (iter.hasNext()) {
            final DaemonSession cur = iter.next();
            if (cur.getTimeSinceCreation() > MAX_WAIT_TIME_FOR_SECOND_CHANNEL) {
                iter.remove();
            }
        }
    }

}
