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

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ReceiverCommandBuilder extends CommandBuilder {

    private final ReceiverCommandWriter writer;

    private ReceiverCommandBuilder() {
        this.writer = new ReceiverCommandWriter(new DataOutputStream(this.getBuffer()));
    }

    public static ReceiverCommandBuilder start() {
        return new ReceiverCommandBuilder();
    }

    public ReceiverCommandBuilder startFile(int index) throws IOException {
        this.writer.writeFileStart(index);
        return this;
    }

    public ReceiverCommandBuilder endFile(byte[] checksum) throws IOException {
        this.writer.writeFileEnd(checksum);
        return this;
    }

    public ReceiverCommandBuilder rawData(String string) throws IOException {
        final byte[] data = TestHelper.toIso(string);
        this.writer.writeRawData(data.length, new ByteArrayInputStream(data));
        return this;
    }

    public ReceiverCommandBuilder copyBlock(long startOffset, int length) throws IOException {
        assert length <= Short.MAX_VALUE;
        this.writer.writeCopyBlock(startOffset, (short) length);
        return this;
    }

    public ReceiverCommandBuilder enumeratorDone() throws IOException {
        this.writer.writeEnumeratorDone();
        return this;
    }

}
