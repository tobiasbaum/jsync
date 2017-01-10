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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FilePathAdapter implements FilePath {

    private final File file;

    public FilePathAdapter(File file) {
        this.file = file;
    }

    @Override
    public String getName() {
        return this.file.getName();
    }

    @Override
    public FilePath getParent() {
        final File p = this.file.getParentFile();
        return p == null ? null : new FilePathAdapter(p);
    }

    @Override
    public Iterable<? extends FilePath> getChildrenSorted() throws IOException {
        final File[] children = this.file.listFiles();
        if (children == null) {
            throw new IOException("Could not determine children of " + this.file + ". Does it exist?");
        }
        Arrays.sort(children, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        final ArrayList<FilePath> ret = new ArrayList<FilePath>();
        for (final File f : children) {
            ret.add(new FilePathAdapter(f));
        }
        return ret;
    }

    @Override
    public FilePath getChild(String name) {
        return new FilePathAdapter(this.createSubfile(name));
    }

    private File createSubfile(String name) {
        return new File(this.file, name);
    }

    @Override
    public boolean hasChild(String name) {
        return this.createSubfile(name).exists();
    }

    @Override
    public boolean isDirectory() {
        //TODO X: Auf performantere Methode umsteigen, sobald Java 7 möglich
        return this.file.isDirectory();
    }

    @Override
    public long getSize() {
        return this.file.length();
    }

    @Override
    public long getLastChange() {
        return this.file.lastModified();
    }

    @Override
    public void setLastChange(long lastChange) throws IOException {
        final boolean success = this.file.setLastModified(lastChange);
        if (!success) {
            throw new IOException("setting last modified failed for " + this.file);
        }
    }

    @Override
    public FilePath createSubdirectory(String name) throws IOException {
        final File sf = this.createSubfile(name);
        final boolean created = sf.mkdir();
        if (!created && !sf.exists()) {
            throw new IOException("could not create " + sf);
        }
        return new FilePathAdapter(sf);
    }

    @Override
    public void delete() throws IOException {
        if (this.isDirectory()) {
            for (final FilePath child : this.getChildrenSorted()) {
                child.delete();
             }
        }
        final boolean deleted = this.file.delete();
        if (!deleted) {
            throw new IOException("could not delete " + this.file);
        }
    }

    @Override
    public void renameTo(String newName) throws IOException {
        final File newFile = new File(this.file.getParentFile(), newName);
        newFile.delete();
        final boolean success = this.file.renameTo(newFile);
        if (!success) {
            throw new IOException("could not rename " + this.file + " to " + newName);
        }
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new FileInputStream(this.file);
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        return new FileOutputStream(this.file);
    }

    @Override
    public RandomAccessInput openRandomAccessInput() throws IOException {
        return new RandomAccessFileInput(this.file);
    }

}
