/*
    Copyright (C) 2013-2017  Tobias Baum <tbaum at tntinteractive.de>

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DaemonSession {

    private static final ScheduledThreadPoolExecutor killerExecutor = new ScheduledThreadPoolExecutor(1);

    private final int sessionId;
    private final long creationTime;
    private final FilePath remoteParentDir;

    private InputStream ch1in;
    private InputStream ch2in;
    private OutputStream ch2out;


    public DaemonSession(int sessionId, FilePath remoteParentDir) {
        this.sessionId = sessionId;
        this.creationTime = System.currentTimeMillis();
        this.remoteParentDir = remoteParentDir;
    }

    public void addFirstChannel(Socket bound, InputStream in, OutputStream out) {
        this.ch1in = in;
    }

    public void addSecondChannel(Socket bound, InputStream in, OutputStream out) {
        this.ch2in = in;
        this.ch2out = out;

        final LinkedBlockingQueue<Integer> toResend = new LinkedBlockingQueue<Integer>();
        final FastConcurrentList<TargetFileInfo> filePaths = new FastConcurrentList<TargetFileInfo>();

        final Receiver receiver = new Receiver(this.ch2in, filePaths, toResend);
        final Thread rt = new Thread(receiver, "receiver" + this.sessionId);
        rt.start();

        final Generator generator = new Generator(this.ch1in, this.remoteParentDir, toResend, this.ch2out, filePaths);
        final Thread gt = new Thread(generator, "generator" + this.sessionId);
        gt.start();

        //nach spätestens 12 Stunden wird die Session hart abgeschossen
        final Runnable killer = new Runnable() {
            private int callCount;
            private final WeakReference<Thread> rec = new WeakReference<Thread>(rt);
            private final WeakReference<Thread> gen = new WeakReference<Thread>(gt);

            @Override
            public void run() {
                if (this.callCount < 12) {
                    if (this.rec.get() != null || this.gen.get() != null) {
                        //Session existiert noch, 1 ner Stunde nochmal prüfen
                        this.callCount++;
                        killerExecutor.schedule(this, 1, TimeUnit.HOURS);
                    }
                    return;
                }
                Logger.LOGGER.warning("killing session " + DaemonSession.this.sessionId + " due to timeout");
                this.killThread(this.rec.get());
                this.killThread(this.gen.get());
                this.closeSocket(DaemonSession.this.ch1in);
                this.closeSocket(DaemonSession.this.ch2in);
            }

            private void closeSocket(InputStream ch1in) {
                try {
                    DaemonSession.this.ch1in.close();
                } catch (final IOException e) {
                    Logger.LOGGER.log(Level.WARNING, "exception while closing socket ", e);
                }
            }

            private void killThread(Thread thread) {
                if (thread == null) {
                    return;
                }
                thread.interrupt();
            }
        };
        killerExecutor.schedule(killer, 1, TimeUnit.HOURS);
    }

    public long getTimeSinceCreation() {
        return System.currentTimeMillis() - this.creationTime;
    }

}
