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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class ReceiverCommandWriter {

    private final DataOutputStream output;

    public ReceiverCommandWriter(DataOutputStream target) {
        this.output = target;
    }

    public void writeFileStart(int index) throws IOException {
        this.output.writeByte(ReceiverCommand.FILE_START.getCode());
        this.output.writeInt(index);
    }

    public void writeFileEnd(byte[] hash) throws IOException {
        this.output.writeByte(ReceiverCommand.FILE_END.getCode());
        this.output.write(hash);
    }

    public void writeRawData(int length, InputStream data) throws IOException {
        this.output.writeByte(ReceiverCommand.RAW_DATA.getCode());
        this.output.writeInt(length);
        StreamHelper.copy(data, this.output, length);
    }

    public void writeCopyBlock(long startOffset, short length) throws IOException {
        this.output.writeByte(ReceiverCommand.COPY_BLOCK.getCode());
        this.output.writeLong(startOffset);
        this.output.writeShort(length);
    }

    public void writeEnumeratorDone() throws IOException {
        this.output.writeByte(ReceiverCommand.ENUMERATOR_DONE.getCode());
    }

    public void close() {
        try {
            this.output.close();
        } catch (final IOException e) {
            Logger.LOGGER.log(Level.WARNING, "error while closing", e);
        }
    }

}
