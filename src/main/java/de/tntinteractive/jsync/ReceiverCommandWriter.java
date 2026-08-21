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

import java.io.InputStream;

public class ReceiverCommandWriter {
  private final DataTarget output;
  
  public ReceiverCommandWriter(DataTarget target) {
    this.output = target;
  }
  
  public void writeFileStart(int index) throws Exception {
    this.output.writeCommand(CommandBuilder.of(ReceiverCommand.FILE_START.getCode())
        .writeInt(index));
  }
  
  public void writeFileEnd(byte[] hash) throws Exception {
    this.output.writeCommand(CommandBuilder.of(ReceiverCommand.FILE_END.getCode())
        .write(hash));
  }
  
  public void writeRawData(int length, InputStream data) throws Exception {
    this.output.writeCommand(ReceiverCommand.RAW_DATA.getCode(), length, data);
  }
  
  public void writeCopyBlock(long startOffset, short length) throws Exception {
    this.output.writeCommand(CommandBuilder.of(ReceiverCommand.COPY_BLOCK.getCode())
        .writeLong(startOffset)
        .writeShort(length));
  }
  
  public void writeEnumeratorDone() throws Exception {
    this.output.writeCommand(CommandBuilder.of(ReceiverCommand.ENUMERATOR_DONE.getCode()));
  }
}
