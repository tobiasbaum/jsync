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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.TreeMap;

public class StubFilePath implements FilePath {

    private final StubFilePath parent;
    private String name;
    private final boolean isDir;
    private final TreeMap<String, StubFilePath> children;
    private final long size;
    private long lastChange;
    private byte[] content;

    /**
     * Konstruktor für Verzeichnis.
     */
    public StubFilePath(StubFilePath parent, String dirName) {
        this.parent = parent;
        this.name = dirName;
        this.children = new TreeMap<String, StubFilePath>();
        this.isDir = true;
        this.size = 0;
        this.lastChange = 0;

        if (parent != null) {
            parent.children.put(dirName, this);
        }
    }

    /**
     * Konstruktor für Datei.
     */
    public StubFilePath(StubFilePath parent, String filename, long size, long lastChange) {
        this.parent = parent;
        this.name = filename;
        this.children = null;
        this.isDir = false;
        this.size = size;
        this.lastChange = lastChange;

        if (parent != null) {
            parent.children.put(filename, this);
        }
    }

    public StubFilePath(StubFilePath parent, String name, String content) {
        this(parent, name, content.length(), 42);
        this.content = TestHelper.toIso(content);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Iterable<? extends FilePath> getChildrenSorted() throws IOException {
        if (this.getParent() != null && !this.getParent().hasChild(this.getName())) {
            throw new IOException(this.name + " does not exist");
        }
        return new ArrayList<StubFilePath>(this.children.values());
    }

    @Override
    public boolean isDirectory() {
        return this.isDir;
    }

    @Override
    public StubFilePath getParent() {
        return this.parent;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public long getLastChange() {
        return this.lastChange;
    }

    @Override
    public StubFilePath getChild(String name) {
        if (this.children.containsKey(name)) {
            return this.children.get(name);
        } else {
            final StubFilePath ret = new StubFilePath(this, name, 0, 0);
            this.children.remove(name);
            return ret;
        }
    }

    @Override
    public boolean hasChild(String name) {
        return this.children.containsKey(name);
    }

    @Override
    public StubFilePath createSubdirectory(String name) {
        assert this.isDirectory();
        return new StubFilePath(this, name);
    }

    @Override
    public void delete() {
        this.parent.children.remove(this.getName());
    }

    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(this.content);
    }

    public String getContent() {
        return TestHelper.fromIso(this.content);
    }

    @Override
    public void renameTo(String newName) {
        this.getParent().children.remove(this.name);
        this.name = newName;
        this.getParent().children.put(newName, this);
    }

    @Override
    public OutputStream openOutputStream() {
        return new OutputStream() {
            private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            @Override
            public void write(int b) throws IOException {
                this.buffer.write(b);
            }

            @Override
            public void close() throws IOException {
                this.buffer.close();
                StubFilePath.this.content = this.buffer.toByteArray();
            }
        };
    }

    @Override
    public void setLastChange(long lastChange) throws IOException {
        this.lastChange = lastChange;
    }

    @Override
    public RandomAccessInput openRandomAccessInput() throws IOException {
        return new RandomAccessInput() {

            @Override
            public void copyTo(OutputStream target, long offset, short length) throws IOException {
                target.write(StubFilePath.this.content, (int) offset, length);
            }

            @Override
            public void close() throws IOException {
            }
        };
    }

}
