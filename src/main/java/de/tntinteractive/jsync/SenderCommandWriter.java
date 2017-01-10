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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

class SenderCommandWriter {

    private final DataOutputStream output;

    public SenderCommandWriter(final DataOutputStream output) {
        this.output = output;
    }

    void writeFileStart(final int index, final int strongHashSize, final int blockSize) throws IOException {
        assert strongHashSize <= Byte.MAX_VALUE;
        assert blockSize <= Short.MAX_VALUE;
        this.output.writeByte(SenderCommand.FILE_START.getCode());
        this.output.writeInt(index);
        this.output.writeByte(strongHashSize);
        this.output.writeShort(blockSize);
    }

    void writeHashes(final int rollingHash, final byte[] strongHash) throws IOException {
        this.output.writeByte(SenderCommand.HASH.getCode());
        this.output.writeInt(rollingHash);
        this.output.write(strongHash);
    }

    void writeFileEnd() throws IOException {
        this.output.writeByte(SenderCommand.FILE_END.getCode());
    }

    void writeEnumeratorDone() throws IOException {
        this.output.writeByte(SenderCommand.ENUMERATOR_DONE.getCode());
    }

    void writeEverythingOk() throws IOException {
        this.output.writeByte(SenderCommand.EVERYTHING_OK.getCode());
    }

    public void close() {
        try {
            this.output.close();
        } catch (final IOException e) {
            Logger.LOGGER.log(Level.WARNING, "error while closing", e);
        }
    }

}
