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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CommandBuilder {
  private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
  
  private final DataOutputStream outputStream = new DataOutputStream(this.bytes);

  public static CommandBuilder of(byte command) {
    return of((int) command);
  }

  public static CommandBuilder of(int command) {
    CommandBuilder ret = new CommandBuilder();
    try {
      ret.outputStream.writeByte(command);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    return ret;
  }
  
  public byte[] toBytes() {
    try {
      this.outputStream.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    return this.bytes.toByteArray();
  }
  
  public CommandBuilder writeUTF(String name) {
    try {
      this.outputStream.writeUTF(name);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    return this;
  }
  
  public CommandBuilder writeLong(long size) {
    try {
      this.outputStream.writeLong(size);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    return this;
  }
  
  public CommandBuilder writeShort(short length) {
    try {
      this.outputStream.writeShort(length);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    return this;
  }
  
  public CommandBuilder write(byte[] hash) {
    try {
      this.outputStream.write(hash);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    return this;
  }
  
  public CommandBuilder writeInt(int index) {
    try {
      this.outputStream.writeInt(index);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    return this;
  }
  
  public CommandBuilder writeByte(int strongHashSize) {
    try {
      this.outputStream.writeByte(strongHashSize);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } 
    return this;
  }
}
