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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

public class MD4StreamTest {

    @Test
    public void testReadBytes() throws Exception {
        final byte[] input = TestHelper.toIso("dies ist ein test");
        final MD4InputStream md4 = new MD4InputStream(new ByteArrayInputStream(input));
        for (final byte element : input) {
            assertEquals(element, md4.read());
        }
        assertEquals(-1, md4.read());

        assertEquals(
                TestHelper.toHexString(TestHelper.md4("dies ist ein test")),
                TestHelper.toHexString(md4.getDigest()));
    }

    @Test
    public void testReadByteArray() throws Exception {
        final byte[] input = TestHelper.toIso("dies ist ein test");
        final MD4InputStream md4 = new MD4InputStream(new ByteArrayInputStream(input));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamHelper.copy(md4, output, input.length);

        assertEquals(
                TestHelper.toHexString(input),
                TestHelper.toHexString(output.toByteArray()));

        assertEquals(
                TestHelper.toHexString(TestHelper.md4("dies ist ein test")),
                TestHelper.toHexString(md4.getDigest()));
    }

    @Test
    public void testReadByteArrayMultipleTimes() throws Exception {
        final byte[] input = TestHelper.toIso("dies ist ein test");
        final MD4InputStream md4 = new MD4InputStream(new ByteArrayInputStream(input));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamHelper.copy(md4, output, 10);
        StreamHelper.copy(md4, output, 10);
        StreamHelper.copy(md4, output, 10);

        assertEquals(
                TestHelper.toHexString(input),
                TestHelper.toHexString(output.toByteArray()));

        assertEquals(
                TestHelper.toHexString(TestHelper.md4("dies ist ein test")),
                TestHelper.toHexString(md4.getDigest()));
    }

    @Test
    public void testReadWithBufferedStream() throws Exception {
        final byte[] input = TestHelper.toIso("dies ist ein test");
        final MD4InputStream md4 = new MD4InputStream(new ByteArrayInputStream(input));
        final BufferedInputStream buffered = new BufferedInputStream(md4);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamHelper.copy(buffered, output, input.length);

        assertEquals(
                TestHelper.toHexString(input),
                TestHelper.toHexString(output.toByteArray()));

        assertEquals(
                TestHelper.toHexString(TestHelper.md4("dies ist ein test")),
                TestHelper.toHexString(md4.getDigest()));
    }

    @Test
    public void testWriteBytes() throws Exception {
        final byte[] data = TestHelper.toIso("dies ist ein test");
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final MD4OutputStream md4 = new MD4OutputStream(buffer);
        md4.write(data);
        assertEquals("dies ist ein test", TestHelper.fromIso(buffer.toByteArray()));
        assertEquals(
                TestHelper.toHexString(TestHelper.md4("dies ist ein test")),
                TestHelper.toHexString(md4.getDigest()));
    }

}
