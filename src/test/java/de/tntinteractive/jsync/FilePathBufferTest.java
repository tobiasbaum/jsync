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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class FilePathBufferTest {

    @Test
    public void testFilePathBuffer() {
        final FastConcurrentList<FilePath> b = new FastConcurrentList<FilePath>();
        for (int i = 0; i < 1000; i++) {
            final StubFilePath fp = new StubFilePath(null, "d" + i);
            final int idx = b.add(fp);
            assertEquals(i, idx);
            assertSame(fp, b.get(i));
        }
        for (int i = 0; i < 1000; i++) {
            assertEquals("d" + i, b.get(i).getName());
        }
    }

}
