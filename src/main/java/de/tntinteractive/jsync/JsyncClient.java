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
import java.net.Socket;

public class JsyncClient {

    public static final String FIRST_CHANNEL_HEADER = "JSYNC CH1";
    public static final String SECOND_CHANNEL_HEADER = "JSYNC CH2";

    public static void main(String[] args) {
        System.out.println(getHeader("JsyncClient"));
        try {
            final File localDirectory = parseLocalDirArg(args[0]);
            final String[] hostAndPort = args[1].split(":");
            final int port = Integer.parseInt(hostAndPort[1]);
            new JsyncClient().syncDirectory(localDirectory, hostAndPort[0], port, args[2]);
        } catch (final Throwable e) {
            e.printStackTrace();
            System.out.println("Expected command line: <localDir> <host:port> <targetDir>");
            System.exit(99);
        }
    }

    public static String getHeader(String programName) {
        return programName + " V1.0, Copyright (C) 2013  Tobias Baum";
    }

    private static File parseLocalDirArg(String arg) {
        if (arg.endsWith("/") || arg.endsWith("\\")) {
            return new File(arg + ".");
        } else {
            return new File(arg);
        }
    }

    public void syncDirectory(File localDirectory, String remoteHost, int remotePort,
            String remoteParentDirectory) throws Throwable {

        System.out.println("Syncing from " + localDirectory + " to "
                + remoteHost + ":" + remotePort + " " + remoteParentDirectory);

        final Socket ch1 = new Socket(remoteHost, remotePort);
        ch1.setKeepAlive(true);
        try {
            final InputStream ch1in = ch1.getInputStream();
            final OutputStream ch1out = ch1.getOutputStream();

            final int sessionId = this.initiateSession(ch1in, ch1out, remoteParentDirectory,
                    localDirectory.getName().equals("."));

            final Socket ch2 = new Socket(remoteHost, remotePort);
            ch2.setKeepAlive(true);
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
                System.out.println("JsyncClient is done.");
            } finally {
                ch2.close();
            }
        } finally {
            ch1.close();
        }
    }

    private int initiateSession(InputStream ch1in, OutputStream ch1out, String remoteParentDirectory,
            boolean createDir) throws IOException {
        final DataOutputStream out = new DataOutputStream(ch1out);
        out.writeUTF(FIRST_CHANNEL_HEADER);
        out.writeUTF(remoteParentDirectory);
        out.writeBoolean(createDir);

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
