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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamHelper {

    public static void copy(InputStream data, OutputStream target, int length) throws IOException {
        final byte[] buffer = new byte[8 * 1024];
        int remaining = length;
        while (remaining > 0) {
            final int toRead = Math.min(remaining, buffer.length);
            final int actual = data.read(buffer, 0, toRead);
            if (actual < 0) {
                break;
            }
            target.write(buffer, 0, actual);
            remaining -= actual;
        }
    }

    public static int readFully(InputStream in, byte[] block) throws IOException {
        int readSoFar = 0;
        while (readSoFar < block.length) {
            final int count = in.read(block, readSoFar, block.length - readSoFar);
            if (count < 0) {
                break;
            }
            readSoFar += count;
        }
        return readSoFar;
    }

}
