/*
    Copyright (C) 2013-2017  Tobias Baum <tbaum at tntinteractive.de>

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

/**
 * Abstraktion über Dateisystemiteration (für Testfälle, und um leichter auf die Java 7-Klassen wechseln zu können).
 */
public interface FilePath {

    public abstract String getName();

    public abstract FilePath getParent();

    public abstract Iterable<? extends FilePath> getChildrenSorted() throws IOException;

    public abstract FilePath getChild(String string);

    public abstract boolean hasChild(String name);

    public abstract boolean isDirectory();

    public abstract long getSize();

    public abstract long getLastChange();

    public abstract void setLastChange(long lastChange) throws IOException;

    public abstract FilePath createSubdirectory(String name) throws IOException;

    public abstract void delete() throws IOException;

    public abstract void renameTo(String substring) throws IOException;

    public abstract InputStream openInputStream() throws IOException;

    public abstract OutputStream openOutputStream() throws IOException;

    public abstract RandomAccessInput openRandomAccessInput() throws IOException;

}
