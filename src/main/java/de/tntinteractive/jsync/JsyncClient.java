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
        } catch (final Throwable e) {
            e.printStackTrace();
            System.out.println("Expected command line: <localDir> <host:port> <targetDir>");
            System.exit(99);
        }
    }

    public void syncDirectory(File localDirectory, String remoteHost, int remotePort,
            String remoteParentDirectory) throws Throwable {

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

                final FastConcurrentList<FilePath> filePaths = new FastConcurrentList<FilePath>();
                final ExceptionBuffer exc = new ExceptionBuffer();

                final Sender sender = new Sender(ch2in, filePaths, ch2out, exc);
                final Thread st = new Thread(sender, "sender");
                st.start();

                final Enumerator enumerator = new Enumerator(new FilePathAdapter(localDirectory), ch1out, filePaths, exc);
                final Thread et = new Thread(enumerator, "enumerator");
                et.start();

                et.join();
                st.join();

                exc.doHandling();
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
        final int sessionId = in.readInt();
        if (sessionId < 0) {
            throw new IOException("Error while initiating session: " + in.readUTF());
        }
        return sessionId;
    }

    private void initSecondChannel(OutputStream ch2out, int sessionId) throws IOException {
        final DataOutputStream out = new DataOutputStream(ch2out);
        out.writeUTF(SECOND_CHANNEL_HEADER);
        out.writeInt(sessionId);
    }

}
