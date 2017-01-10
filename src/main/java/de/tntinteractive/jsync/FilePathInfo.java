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

public class FilePathInfo {
    private final FilePathInfo parent;
    private final String name;
    private final boolean isDir;

    public FilePathInfo(FilePathInfo parent, String name) {
        this.parent = parent;
        this.name = name;
        this.isDir = true;
    }

    public String getName() {
        return this.name;
    }

    public FilePathInfo getParent() {
        return this.parent;
    }

    public boolean isDirectory() {
        return this.isDir;
    }
}