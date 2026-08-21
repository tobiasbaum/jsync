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
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class StreamDataTarget implements DataTarget {
  public synchronized void writeCommand(CommandBuilder code) throws IOException, ExecutionException, InterruptedException, TimeoutException {
    getStream().write(code.toBytes());
  }
  
  public synchronized void writeCommand(byte code, int length, InputStream data) throws IOException, ExecutionException, InterruptedException, TimeoutException {
    OutputStream stream = getStream();
    stream.write(code);
    stream.write(length >>> 24 & 0xFF);
    stream.write(length >>> 16 & 0xFF);
    stream.write(length >>> 8 & 0xFF);
    stream.write(length >>> 0 & 0xFF);
    StreamHelper.copy(data, stream, length);
  }
  
  public synchronized void writeNop() throws IOException, ExecutionException, InterruptedException, TimeoutException {
    getStream().write(0);
  }
  
  protected abstract OutputStream getStream() throws IOException, ExecutionException, InterruptedException, TimeoutException;
}
