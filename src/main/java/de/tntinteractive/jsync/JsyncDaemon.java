package de.tntinteractive.jsync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        ServerSocket s = null;
        try {
            final int port = Integer.parseInt(args[0]);
            s = new ServerSocket(port);
        } catch (final Exception e) {
            Logger.LOGGER.info("command line: <port>");
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
            final File dir = new File(remoteParentDir);
            if (!dir.exists()) {
                final String msg = "Target directory does not exist: " + remoteParentDir;
                Logger.LOGGER.warning(msg);
                dOut.writeInt(-1);
                dOut.writeUTF(msg);
                bound.close();
                return;
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
