package de.tntinteractive.jsync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class DaemonSession {
  private static final int CONNECT_TIMEOUT_SECONDS = 30;
  
  private static final ScheduledThreadPoolExecutor killerExecutor = new ScheduledThreadPoolExecutor(1);
  
  private final long sessionId;
  
  private final long creationTime;
  
  private final CompletableFuture<Socket>[] sockets = (CompletableFuture<Socket>[])new CompletableFuture[3];
  
  private final Runnable closeCallback;
  
  public DaemonSession(long sessionId, FilePath remoteParentDir, Runnable closeCallback) {
    this.sessionId = sessionId;
    this.creationTime = System.currentTimeMillis();
    this.closeCallback = closeCallback;
    this.sockets[1] = new CompletableFuture<>();
    this.sockets[2] = new CompletableFuture<>();
    LinkedBlockingQueue<Integer> toResend = new LinkedBlockingQueue<>();
    FastConcurrentList<TargetFileInfo> filePaths = new FastConcurrentList<>();
    DataSource s2 = new SocketInput(2);
    Receiver receiver = new Receiver(s2, filePaths, toResend, this::close);
    final Thread rt = new Thread(receiver, "receiver" + this.sessionId);
    rt.start();
    DataTarget ch1out = new StreamDataTarget() {
        protected OutputStream getStream() throws IOException, ExecutionException, InterruptedException, TimeoutException {
          return ((Socket)DaemonSession.this.sockets[1].get(30L, TimeUnit.SECONDS)).getOutputStream();
        }
      };
    Generator generator = new Generator(new SocketInput(1), remoteParentDir, toResend, ch1out, filePaths, this::close);
    final Thread gt = new Thread(generator, "generator" + this.sessionId);
    gt.start();
    Runnable killer = new Runnable() {
        private int callCount;
        
        private final WeakReference<Thread> rec = new WeakReference<>(rt);
        
        private final WeakReference<Thread> gen = new WeakReference<>(gt);
        
        public void run() {
          if (this.callCount < 12) {
            if (this.rec.get() != null || this.gen.get() != null) {
              this.callCount++;
              DaemonSession.killerExecutor.schedule(this, 1L, TimeUnit.HOURS);
            } 
            return;
          } 
          Logger.LOGGER.warning("killing session " + DaemonSession.this.sessionId + " due to timeout");
          killThread(this.rec.get());
          killThread(this.gen.get());
          DaemonSession.this.close();
        }
        
        private void killThread(Thread thread) {
          if (thread == null)
            return; 
          thread.interrupt();
        }
      };
    killerExecutor.schedule(killer, 1L, TimeUnit.HOURS);
  }
  
  private class SocketInput extends StreamDataSource {
    private final int channel;
    
    public SocketInput(int channel) {
      this.channel = channel;
    }
    
    protected InputStream getStream() throws IOException, ExecutionException, InterruptedException, TimeoutException {
      return ((Socket)DaemonSession.this.sockets[this.channel].get(30L, TimeUnit.SECONDS)).getInputStream();
    }
  }
  
  public void connectChannel(int channel, Socket bound) throws IOException {
    assert channel == 1 || channel == 2;
    closeSocket(channel);
    this.sockets[channel].complete(bound);
  }
  
  private void closeSocket(int channel) {
    try {
      Future<Socket> oldFuture = this.sockets[channel];
      if (oldFuture.isDone()) {
        this.sockets[channel] = new CompletableFuture<>();
        ((Socket)oldFuture.get()).close();
      } 
    } catch (IOException|InterruptedException|ExecutionException e) {
      Logger.LOGGER.log(Level.INFO, "exception on close", e);
    } 
  }
  
  public long getTimeSinceCreation() {
    return System.currentTimeMillis() - this.creationTime;
  }
  
  public void close() {
    closeSocket(1);
    closeSocket(2);
    this.closeCallback.run();
  }
}
