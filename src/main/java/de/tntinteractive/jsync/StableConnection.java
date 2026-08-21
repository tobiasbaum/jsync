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

import java.io.Closeable;
import java.io.IOException;

public class StableConnection implements Closeable {
  private ClientSocket ch1;
  
  private ClientSocket ch2;
  
  public StableConnection(String remoteHost, int remotePort, String remoteParentDirectory, boolean createDir) throws IOException {
    InitialSocket is = InitialSocket.initiate(remoteHost, remotePort, remoteParentDirectory, createDir);
    this.ch1 = ClientSocket.fromInitSocket(is);
    this.ch2 = ClientSocket.forSecondChannel(remoteHost, remotePort, is.getSessionId());
  }
  
  public DataTarget getEnumeratorOutput() throws IOException {
    return this.ch1.getDataTarget();
  }
  
  public DataSource getSenderInput() throws IOException {
    return this.ch1.getDataSource();
  }
  
  public DataTarget getSenderOutput() throws IOException {
    return this.ch2.getDataTarget();
  }
  
  public void close() throws IOException {
    if (this.ch1 != null)
      this.ch1.close(); 
    if (this.ch2 != null)
      this.ch2.close(); 
  }
}
