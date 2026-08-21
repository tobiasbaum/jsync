package de.tntinteractive.jsync;

public class Enumerator implements Runnable {
  private final FilePath localDir;
  
  private final GeneratorCommandWriter writer;
  
  private final FastConcurrentList<FilePath> filePaths;
  
  private final ExceptionBuffer exc;
  
  public Enumerator(FilePath localDir, DataTarget target, FastConcurrentList<FilePath> filePathBuffer, ExceptionBuffer exc) {
    this.localDir = localDir;
    this.writer = new GeneratorCommandWriter(target);
    this.filePaths = filePathBuffer;
    this.exc = exc;
  }
  
  public void run() {
    try {
      int count = sendDirRecursive(this.localDir);
      this.writer.writeEnd();
      System.out.println("Enumerated " + count + " files.");
    } catch (Exception e) {
      this.exc.addThrowable(e);
    } 
  }
  
  private int sendDirRecursive(FilePath dir) throws Exception {
    int count = 0;
    this.writer.writeStepDown(dir.getName());
    for (FilePath child : dir.getChildrenSorted()) {
      if (child.isDirectory()) {
        count += sendDirRecursive(child);
        continue;
      } 
      this.filePaths.add(child);
      this.writer.writeFile(child.getName(), child.getSize(), child.getLastChange());
      count++;
    } 
    this.writer.writeStepUp();
    return count;
  }
}
