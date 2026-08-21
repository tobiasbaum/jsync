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

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

public class Generator implements Runnable {
  private static final String MAX_FILENAME = "￿￿￿";
  
  private final DataSource input;
  
  private final FilePath localParentDir;
  
  private final SenderCommandWriter writer;
  
  private final FastConcurrentList<TargetFileInfo> sourceFilePaths;
  
  private final BlockingQueue<Integer> toResend;
  
  private int strongHashSize = 4;
  
  private final Runnable doneCallback;
  
  private static class GeneratorCommandData {
    private final GeneratorCommand type;
    
    private final String name;
    
    private final long size;
    
    private final long lastChange;
    
    public GeneratorCommandData(GeneratorCommand type, String name, long size, long lastChange) {
      this.type = type;
      this.name = name;
      this.size = size;
      this.lastChange = lastChange;
    }
    
    public GeneratorCommand getType() {
      return this.type;
    }
    
    public String getName() {
      return this.name;
    }
    
    public long getSize() {
      return this.size;
    }
    
    public long getLastChange() {
      return this.lastChange;
    }
  }
  
  private static class GeneratorCommandIterator implements ExplicitMoveIterator<GeneratorCommandData> {
    private final DataSource input;
    
    private Generator.GeneratorCommandData current;
    
    private boolean eof;
    
    public GeneratorCommandIterator(DataSource input) throws Exception {
      this.input = input;
      move();
    }
    
    public Generator.GeneratorCommandData get() {
      return this.current;
    }
    
    public void move() throws Exception {
      if (this.eof)
        return; 
      while (true) {
        CommandReader command = this.input.readCommand();
        if (command == null || command.code == GeneratorCommand.END.getCode()) {
          Logger.LOGGER.log(Level.FINE, "generator got end command " + command);
          this.current = null;
          this.eof = true;
          return;
        } 
        if (command.code != GeneratorCommand.NOP.getCode()) {
          Logger.LOGGER.log(Level.FINE, "generator got command " + command.code);
          if (command.code == GeneratorCommand.FILE.getCode()) {
            String name = command.input.readUTF();
            long size = command.input.readLong();
            long changedAt = command.input.readLong();
            this.current = new Generator.GeneratorCommandData(GeneratorCommand.FILE, name, size, changedAt);
          } else if (command.code == GeneratorCommand.STEP_DOWN.getCode()) {
            String name = command.input.readUTF();
            this.current = new Generator.GeneratorCommandData(GeneratorCommand.STEP_DOWN, name, -1L, -1L);
          } else if (command.code == GeneratorCommand.STEP_UP.getCode()) {
            this.current = new Generator.GeneratorCommandData(GeneratorCommand.STEP_UP, null, -1L, -1L);
          } else {
            throw new IOException("unknown command " + command.code);
          } 
          return;
        } 
      } 
    }
    
    public boolean hasCurrent() {
      return (this.current != null);
    }
  }
  
  public Generator(DataSource source, FilePath remoteParentDir, BlockingQueue<Integer> toResend, DataTarget target, FastConcurrentList<TargetFileInfo> filePaths, Runnable doneCallback) {
    this.input = source;
    this.localParentDir = remoteParentDir;
    this.toResend = toResend;
    this.writer = new SenderCommandWriter(target);
    this.sourceFilePaths = filePaths;
    this.doneCallback = doneCallback;
  }
  
  public void run() {
    try {
      FilePath baseDir;
      ExplicitMoveIterator<GeneratorCommandData> commandIter = new GeneratorCommandIterator(this.input);
      GeneratorCommandData baseDirCommand = commandIter.get();
      commandIter.move();
      sanityCheck((baseDirCommand.getType() == GeneratorCommand.STEP_DOWN));
      if (this.localParentDir.hasChild(baseDirCommand.getName())) {
        baseDir = this.localParentDir.getChild(baseDirCommand.getName());
        if (!baseDir.isDirectory()) {
          baseDir.delete();
          baseDir = this.localParentDir.createSubdirectory(baseDirCommand.getName());
        } 
      } else {
        baseDir = this.localParentDir.createSubdirectory(baseDirCommand.getName());
      } 
      mergeRecursive(baseDir, commandIter);
      Logger.LOGGER.log(Level.FINE, "generator: mergeRecursive done");
      sanityCheck(!commandIter.hasCurrent());
      this.writer.writeEnumeratorDone();
      this.strongHashSize++;
      int lastIndex = -1;
      while (!Thread.interrupted()) {
        int index = ((Integer)this.toResend.take()).intValue();
        Logger.LOGGER.log(Level.FINE, "generator: to resend " + index);
        if (index < 0)
          break; 
        if (index <= lastIndex)
          this.strongHashSize++; 
        lastIndex = index;
        writeCopyCommandForMissingFile(index);
      } 
      Logger.LOGGER.log(Level.FINE, "generator done");
      this.writer.writeEverythingOk();
      this.doneCallback.run();
    } catch (InterruptedException e) {
      Logger.LOGGER.log(Level.SEVERE, "generator interrupted", e);
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      Logger.LOGGER.log(Level.SEVERE, "exception in generator", e);
      this.doneCallback.run();
    } 
  }
  
  private void mergeRecursive(FilePath localDir, ExplicitMoveIterator<GeneratorCommandData> commandIter) throws Exception {
    ExplicitMoveIterator<FilePath> childrenIter = new ExplicitMoveAdapter<>(localDir.getChildrenSorted());
    while (!Thread.currentThread().isInterrupted()) {
      int index;
      GeneratorCommandData currentCommand = commandIter.get();
      String remoteName = currentCommand.getName();
      String localName = childrenIter.hasCurrent() ? ((FilePath)childrenIter.get()).getName() : "￿￿￿";
      switch (currentCommand.getType()) {
        case STEP_DOWN:
          if (remoteName.compareTo(localName) < 0) {
            FilePath subdir = localDir.createSubdirectory(remoteName);
            commandIter.move();
            createAllRecursive(subdir, commandIter);
            continue;
          } 
          if (remoteName.compareTo(localName) > 0) {
            ((FilePath)childrenIter.get()).delete();
            childrenIter.move();
            continue;
          } 
          if (((FilePath)childrenIter.get()).isDirectory()) {
            commandIter.move();
            mergeRecursive(childrenIter.get(), commandIter);
          } else {
            ((FilePath)childrenIter.get()).delete();
            FilePath subdir = localDir.createSubdirectory(remoteName);
            commandIter.move();
            createAllRecursive(subdir, commandIter);
          } 
          childrenIter.move();
          continue;
        case FILE:
          if (remoteName.compareTo(localName) < 0) {
            int i = this.sourceFilePaths.add(new TargetFileInfo(localDir
                  .getChild(remoteName), currentCommand.getLastChange()));
            writeCopyCommandForMissingFile(i);
            commandIter.move();
            continue;
          } 
          if (remoteName.compareTo(localName) > 0) {
            ((FilePath)childrenIter.get()).delete();
            childrenIter.move();
            continue;
          } 
          index = this.sourceFilePaths.add(new TargetFileInfo(localDir
                .getChild(remoteName), currentCommand.getLastChange()));
          if (((FilePath)childrenIter.get()).isDirectory())
            throw new RuntimeException(); 
          if (((GeneratorCommandData)commandIter.get()).getSize() != ((FilePath)childrenIter.get()).getSize() || ((GeneratorCommandData)commandIter
            .get()).getLastChange() != ((FilePath)childrenIter.get()).getLastChange())
            writeCopyCommandForExistingFile(index); 
          commandIter.move();
          childrenIter.move();
          continue;
        case STEP_UP:
          commandIter.move();
          while (childrenIter.hasCurrent()) {
            ((FilePath)childrenIter.get()).delete();
            childrenIter.move();
          } 
          return;
      } 
      throw new IOException("unknown type " + currentCommand.getType());
    } 
  }
  
  private void writeCopyCommandForExistingFile(int index) throws Exception {
    int blockSize = 2044 + this.strongHashSize;
    this.writer.writeFileStart(index, this.strongHashSize, blockSize);
    InputStream in = ((TargetFileInfo)this.sourceFilePaths.get(index)).getFilePath().openInputStream();
    try {
      sendHashes(in, blockSize);
    } finally {
      in.close();
    } 
    this.writer.writeFileEnd();
  }
  
  private void sendHashes(InputStream in, int blockSize) throws Exception {
    byte[] block = new byte[blockSize];
    while (StreamHelper.readFully(in, block) == block.length) {
      int rollingHash = Checksum32.determineFor(block);
      byte[] strongHash = MD4.determineFor(block, this.strongHashSize);
      this.writer.writeHashes(rollingHash, strongHash);
    } 
  }
  
  private void writeCopyCommandForMissingFile(int index) throws Exception {
    this.writer.writeFileStart(index, this.strongHashSize, 0);
    this.writer.writeFileEnd();
  }
  
  private void createAllRecursive(FilePath dir, ExplicitMoveIterator<GeneratorCommandData> commandIter) throws Exception {
    while (true) {
      FilePath subdir;
      int index;
      GeneratorCommandData cur = commandIter.get();
      switch (cur.getType()) {
        case STEP_DOWN:
          subdir = dir.createSubdirectory(cur.getName());
          commandIter.move();
          createAllRecursive(subdir, commandIter);
          continue;
        case FILE:
          index = this.sourceFilePaths.add(new TargetFileInfo(dir
                .getChild(cur.getName()), cur.getLastChange()));
          writeCopyCommandForMissingFile(index);
          commandIter.move();
          continue;
        case STEP_UP:
          commandIter.move();
          return;
      } 
      break;
    } 
    throw new IOException("should not happen");
  }
  
  private static void sanityCheck(boolean b) throws IOException {
    if (!b)
      throw new IOException("protocol failure"); 
  }
}
