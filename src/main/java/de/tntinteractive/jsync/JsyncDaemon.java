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
import java.util.Map;

/**
 * Hauptklasse für den Daemon-Teil.
 * Der Daemon muss auf dem Zielsystem gestartet werden und nimmt dann Anfragen entgegen.
 */
public class JsyncDaemon {

    //TODO sessions aufräumen
    private static int lastId;
    private static final Map<Integer, DaemonSession> sessions = new HashMap<Integer, DaemonSession>();

    public static void main(String[] args) {
        try {
            final int port = Integer.parseInt(args[0]);

            final ServerSocket s = new ServerSocket(port);
            Socket bound;
            while (true) {
                bound = s.accept();
                handleConnection(bound);
            }
        } catch (final Exception e) {
            Logger.LOGGER.info("command line: <port>");
            e.printStackTrace();
            System.exit(99);
        }

    }

    private static void handleConnection(Socket bound) throws IOException {
        final InputStream in = bound.getInputStream();
        final OutputStream out = bound.getOutputStream();
        final DataInputStream dIn = new DataInputStream(in);
        final String header = dIn.readUTF();
        if (header.equals(JsyncClient.FIRST_CHANNEL_HEADER)) {
            final String remoteParentDir = dIn.readUTF();
            final File dir = new File(remoteParentDir);
            if (!dir.exists()) {
                Logger.LOGGER.warning("Zielverzeichnis existiert nicht: " + remoteParentDir);
                bound.close();
                return;
            }

            final int sessionId = lastId++;
            if (sessions.containsKey(sessionId)) {
                Logger.LOGGER.warning("sessionId overflow: " + sessionId);
                bound.close();
                return;
            }

            final DaemonSession session = new DaemonSession(sessionId, new FilePathAdapter(dir));
            sessions.put(sessionId, session);

            session.addFirstChannel(bound, in, out);

            final DataOutputStream dOut = new DataOutputStream(out);
            dOut.writeInt(sessionId);
        } else if (header.equals(JsyncClient.SECOND_CHANNEL_HEADER)) {
            final int sessionId = dIn.readInt();
            final DaemonSession session = sessions.get(sessionId);
            if (session == null) {
                Logger.LOGGER.warning("connection to missing session attempted: " + sessionId);
                bound.close();
                return;
            }
            session.addSecondChannel(bound, in, out);
        } else {
            Logger.LOGGER.warning("invalid header received: " + header);
            bound.close();
        }
    }

}
