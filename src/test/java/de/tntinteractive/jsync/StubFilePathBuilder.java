/*
    Copyright (C) 2013  Tobias Baum <tbaum at tntinteractive.de>

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

public class StubFilePathBuilder {

    private StubFilePath current;

    private StubFilePathBuilder(StubFilePath stubFilePath) {
        this.current = stubFilePath;
    }

    public static StubFilePathBuilder start(String dirName) {
        return new StubFilePathBuilder(new StubFilePath(null, dirName));
    }

    public StubFilePathBuilder startDir(String dirName) {
        this.current = new StubFilePath(this.current, dirName);
        return this;
    }

    public StubFilePathBuilder endDir() {
        this.current = this.current.getParent();
        return this;
    }

    public StubFilePathBuilder file(String name, long size, long lastChange) {
        new StubFilePath(this.current, name, size, lastChange);
        return this;
    }

    public StubFilePathBuilder file(String name, String content) {
        new StubFilePath(this.current, name, content);
        return this;
    }

    public StubFilePath build() {
        assert this.current.getParent() == null;
        return this.current;
    }

}
