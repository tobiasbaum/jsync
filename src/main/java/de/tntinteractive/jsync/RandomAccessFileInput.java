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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

class RandomAccessFileInput implements RandomAccessInput {

    private final FileChannel f;
    private final ByteBuffer buffer;

    public RandomAccessFileInput(File file) throws IOException {
        this.f = new FileInputStream(file).getChannel();
        this.buffer = ByteBuffer.wrap(new byte[Short.MAX_VALUE]);
    }

    @Override
    public void copyTo(OutputStream target, long offset, short length) throws IOException {
        this.buffer.rewind();
        this.buffer.limit(length);
        this.f.position(offset);
        while (this.buffer.position() < this.buffer.limit()) {
            final int read = this.f.read(this.buffer, offset);
            if (read < 0) {
                break;
            }
        }
        target.write(this.buffer.array(), 0, this.buffer.position());
    }

    @Override
    public void close() throws IOException {
        this.f.close();
    }

}
