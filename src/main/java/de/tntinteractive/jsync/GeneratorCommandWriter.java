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

class GeneratorCommandWriter {
  private final DataTarget output;
  
  public GeneratorCommandWriter(DataTarget output) {
    this.output = output;
  }
  
  void writeStepDown(String name) throws Exception {
    this.output.writeCommand(CommandBuilder.of(GeneratorCommand.STEP_DOWN.getCode())
        .writeUTF(name));
  }
  
  void writeFile(String name, long size, long lastChange) throws Exception {
    this.output.writeCommand(CommandBuilder.of(GeneratorCommand.FILE.getCode())
        .writeUTF(name)
        .writeLong(size)
        .writeLong(lastChange));
  }
  
  void writeStepUp() throws Exception {
    this.output.writeCommand(CommandBuilder.of(GeneratorCommand.STEP_UP.getCode()));
  }
  
  public void writeEnd() throws Exception {
    this.output.writeCommand(CommandBuilder.of(GeneratorCommand.END.getCode()));
  }
}
