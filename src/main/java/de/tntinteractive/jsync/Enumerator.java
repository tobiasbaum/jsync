/*
    Copyright (C) 2013-2026  Tobias Baum <tbaum at tntinteractive.de>

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
