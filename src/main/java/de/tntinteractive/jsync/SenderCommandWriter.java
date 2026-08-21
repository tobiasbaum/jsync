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

import java.util.logging.Level;

class SenderCommandWriter {
  private final DataTarget output;
  
  public SenderCommandWriter(DataTarget output) {
    this.output = output;
  }
  
  void writeFileStart(int index, int strongHashSize, int blockSize) throws Exception {
    assert strongHashSize <= 127;
    assert blockSize <= 32767;
    this.output.writeCommand(CommandBuilder.of(SenderCommand.FILE_START.getCode())
        .writeInt(index)
        .writeByte(strongHashSize)
        .writeShort((short)blockSize));
  }
  
  void writeHashes(int rollingHash, byte[] strongHash) throws Exception {
    this.output.writeCommand(CommandBuilder.of(SenderCommand.HASH.getCode())
        .writeInt(rollingHash)
        .write(strongHash));
  }
  
  void writeFileEnd() throws Exception {
    this.output.writeCommand(CommandBuilder.of(SenderCommand.FILE_END.getCode()));
  }
  
  void writeEnumeratorDone() throws Exception {
    Logger.LOGGER.log(Level.FINE, "write enumerator done");
    this.output.writeCommand(CommandBuilder.of(SenderCommand.ENUMERATOR_DONE.getCode()));
  }
  
  void writeEverythingOk() throws Exception {
    Logger.LOGGER.log(Level.FINE, "write everything ok");
    this.output.writeCommand(CommandBuilder.of(SenderCommand.EVERYTHING_OK.getCode()));
  }
}
