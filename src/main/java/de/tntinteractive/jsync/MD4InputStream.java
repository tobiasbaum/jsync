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

import java.io.IOException;
import java.io.InputStream;

public class MD4InputStream extends InputStream {

    private final InputStream decorated;
    private final MD4 md4;

    public MD4InputStream(InputStream decorated) {
        this.decorated = decorated;
        this.md4 = new MD4();
    }

    @Override
    public int read() throws IOException {
        final int r = this.decorated.read();
        if (r >= 0) {
            this.md4.engineUpdate((byte) r);
        }
        return r;
    }

    @Override
    public int read(byte[] buffer, int off, int len) throws IOException {
        final int actual = this.decorated.read(buffer, off, len);
        if (actual > 0) {
            this.md4.engineUpdate(buffer, off, actual);
        }
        return actual;
    }

    @Override
    public void close() throws IOException {
        this.decorated.close();
    }

    public byte[] getDigest() {
        return this.md4.engineDigest();
    }

}
