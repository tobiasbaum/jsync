package de.tntinteractive.jsync;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class DaemonSession {

    private final int sessionId;
    private final FilePath remoteParentDir;

    private InputStream ch1in;
    private OutputStream ch1out;
    private InputStream ch2in;
    private OutputStream ch2out;


    public DaemonSession(int sessionId, FilePath remoteParentDir) {
        this.sessionId = sessionId;
        this.remoteParentDir = remoteParentDir;
    }

    public void addFirstChannel(Socket bound, InputStream in, OutputStream out) {
        this.ch1in = in;
        this.ch1out = out;
    }

    public void addSecondChannel(Socket bound, InputStream in, OutputStream out) {
        this.ch2in = in;
        this.ch2out = out;

        final LinkedBlockingQueue<Integer> toResend = new LinkedBlockingQueue<Integer>();
        final FilePathBuffer filePaths = new FilePathBuffer();

        final Receiver receiver = new Receiver(this.ch2in, filePaths, toResend);
        final Thread rt = new Thread(receiver, "receiver" + this.sessionId);
        rt.start();

        final Generator generator = new Generator(this.ch1in, this.remoteParentDir, toResend, this.ch2out, filePaths);
        final Thread gt = new Thread(generator, "generator" + this.sessionId);
        gt.start();
    }

}
