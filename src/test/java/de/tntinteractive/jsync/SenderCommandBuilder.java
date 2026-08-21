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

import java.io.DataOutputStream;

public class SenderCommandBuilder extends TestCommandBuilder {

    private final SenderCommandWriter writer;

    private SenderCommandBuilder() {
        this.writer = new SenderCommandWriter(new TestDataTarget(new DataOutputStream(this.getBuffer())));
    }

    public static SenderCommandBuilder start() {
        return new SenderCommandBuilder();
    }

    public SenderCommandBuilder startFile(int index, int strongHashSize, int blockSize) throws Exception {
        this.writer.writeFileStart(index, strongHashSize, blockSize);
        return this;
    }

    public SenderCommandBuilder hash(int rollingChecksum, byte[] shortMD4) throws Exception {
        this.writer.writeHashes(rollingChecksum, shortMD4);
        return this;
    }

    public SenderCommandBuilder endFile() throws Exception {
        this.writer.writeFileEnd();
        return this;
    }

    public SenderCommandBuilder enumeratorDone() throws Exception {
        this.writer.writeEnumeratorDone();
        return this;
    }

    public SenderCommandBuilder everythingOk() throws Exception {
        this.writer.writeEverythingOk();
        return this;
    }

}
