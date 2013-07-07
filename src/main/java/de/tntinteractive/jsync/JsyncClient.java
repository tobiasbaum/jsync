package de.tntinteractive.jsync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class JsyncClient {

    public static final String FIRST_CHANNEL_HEADER = "JSYNC CH1";
    public static final String SECOND_CHANNEL_HEADER = "JSYNC CH2";

    public static void main(String[] args) {
        try {
            final File localDirectory = new File(args[0]);
            final String[] hostAndPort = args[1].split(":");
            final int port = Integer.parseInt(hostAndPort[1]);
            new JsyncClient().syncDirectory(localDirectory, hostAndPort[0], port, args[2]);
        } catch (final Exception e) {
            e.printStackTrace();
            Logger.LOGGER.info("Expected command line: <localDir> <host:port> <targetDir>");
            System.exit(99);
        }
    }

    public void syncDirectory(File localDirectory, String remoteHost, int remotePort,
            String remoteParentDirectory) throws IOException, InterruptedException {

        final Socket ch1 = new Socket(remoteHost, remotePort);
        try {
            final InputStream ch1in = ch1.getInputStream();
            final OutputStream ch1out = ch1.getOutputStream();

            final int sessionId = this.initiateSession(ch1in, ch1out, remoteParentDirectory);

            final Socket ch2 = new Socket(remoteHost, remotePort);
            try {
                final InputStream ch2in = ch2.getInputStream();
                final OutputStream ch2out = ch2.getOutputStream();
                this.initSecondChannel(ch2out, sessionId);

                final FilePathBuffer filePaths = new FilePathBuffer();

                final Sender sender = new Sender(ch2in, filePaths, ch2out);
                final Thread st = new Thread(sender, "sender");
                st.start();

                final Enumerator enumerator = new Enumerator(new FilePathAdapter(localDirectory), ch1out, filePaths);
                final Thread et = new Thread(enumerator, "enumerator");
                et.start();

                et.join();
                st.join();
            } finally {
                ch2.close();
            }
        } finally {
            ch1.close();
        }
    }

    private int initiateSession(InputStream ch1in, OutputStream ch1out, String remoteParentDirectory) throws IOException {
        final DataOutputStream out = new DataOutputStream(ch1out);
        out.writeUTF(FIRST_CHANNEL_HEADER);
        out.writeUTF(remoteParentDirectory);

        final DataInputStream in = new DataInputStream(ch1in);
        return in.readInt();
    }

    private void initSecondChannel(OutputStream ch2out, int sessionId) throws IOException {
        final DataOutputStream out = new DataOutputStream(ch2out);
        out.writeUTF(SECOND_CHANNEL_HEADER);
        out.writeInt(sessionId);
    }

}
