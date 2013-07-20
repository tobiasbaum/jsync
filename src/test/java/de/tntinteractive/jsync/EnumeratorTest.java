/*
    Copyright (C) 2013  Tobias Bimport static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.junit.Test;
stribute it and/or modify
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

import java.io.ByteArrayOutputStream;

import org.junit.Test;


public class EnumeratorTest {

    private static String callEnumerator(StubFilePath p) throws Exception {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ExceptionBuffer exc = new ExceptionBuffer();
        final Enumerator e = new Enumerator(p, buffer, new FastConcurrentList<FilePath>(), exc);
        e.run();
        exc.doHandling();
        return TestHelper.toHexString(buffer.toByteArray());
    }

    @Test
    public void testSendEmptyDirectory() throws Exception {
        final StubFilePath p = StubFilePathBuilder.start("testverz")
                .build();

        final String expected = GeneratorCommandBuilder.start()
                .stepDown("testverz")
                .stepUp()
                .toHexString();

        final String actual = callEnumerator(p);
        assertEquals(expected, actual);
    }

    @Test
    public void testSendDirectoryWithSubdirectories() throws Exception {
        final StubFilePath p = StubFilePathBuilder.start("x")
                .startDir("b")
                .endDir()
                .startDir("a")
                .endDir()
                .build();

        final String expected = GeneratorCommandBuilder.start()
                .stepDown("x")
                .stepDown("a")
                .stepUp()
                .stepDown("b")
                .stepUp()
                .stepUp()
                .toHexString();

        final String actual = callEnumerator(p);
        assertEquals(expected, actual);
    }


    @Test
    public void testSendFiles() throws Exception {
        final StubFilePath p = StubFilePathBuilder.start("x")
                .file("a.txt", 42, 123)
                .file("b.txt", 43, 124)
                .build();

        final String expected = GeneratorCommandBuilder.start()
                .stepDown("x")
                .file("a.txt", 42, 123)
                .file("b.txt", 43, 124)
                .stepUp()
                .toHexString();

        final String actual = callEnumerator(p);
        assertEquals(expected, actual);
    }

}
