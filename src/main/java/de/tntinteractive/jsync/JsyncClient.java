package de.tntinteractive.jsync;

import java.io.File;

public class JsyncClient {
  public static void main(String[] args) {
    System.out.println(getHeader("JsyncClient"));
    try {
      File localDirectory = parseLocalDirArg(args[0]);
      String[] hostAndPort = args[1].split(":");
      int port = Integer.parseInt(hostAndPort[1]);
      (new JsyncClient()).syncDirectory(localDirectory, hostAndPort[0], port, args[2]);
    } catch (Throwable e) {
      e.printStackTrace();
      System.out.println("Expected command line: <localDir> <host:port> <targetDir>");
      System.exit(99);
    } 
  }
  
  public static String getHeader(String programName) {
    return programName + " V1.2, Copyright (C) 2013-2024  Tobias Baum";
  }
  
  private static File parseLocalDirArg(String arg) {
    if (arg.endsWith("/") || arg.endsWith("\\"))
      return new File(arg + "."); 
    return new File(arg);
  }
  
  public void syncDirectory(File localDirectory, String remoteHost, int remotePort, String remoteParentDirectory) throws Exception {
    long startTime = System.currentTimeMillis();
    System.out.println("Syncing from " + localDirectory + " to " + remoteHost + ":" + remotePort + " " + remoteParentDirectory);
    StableConnection connection = new StableConnection(remoteHost, remotePort, remoteParentDirectory, localDirectory.getName().equals("."));
    try {
      FastConcurrentList<FilePath> filePaths = new FastConcurrentList<>();
      ExceptionBuffer exc = new ExceptionBuffer();
      Sender sender = new Sender(connection.getSenderInput(), filePaths, connection.getSenderOutput(), exc);
      Thread st = new Thread(sender, "sender");
      st.start();
      Enumerator enumerator = new Enumerator(new FilePathAdapter(localDirectory), connection.getEnumeratorOutput(), filePaths, exc);
      Thread et = new Thread(enumerator, "enumerator");
      et.start();
      et.join();
      st.join();
      exc.doHandling();
      long time = System.currentTimeMillis() - startTime;
      System.out.println(String.format("JsyncClient is done after %d s", new Object[] { Long.valueOf(time / 1000L) }));
    } finally {
      connection.close();
    } 
  }
}
