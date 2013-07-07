package de.tntinteractive.jsync;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.junit.Test;


public class EnumeratorTest {

    private static String callEnumerator(StubFilePath p, String remoteParentDir) {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final Enumerator e = new Enumerator(p, remoteParentDir, buffer, new FilePathBuffer());
        e.run();
        return TestHelper.toHexString(buffer.toByteArray());
    }

    @Test
    public void testSendEmptyDirectory() throws Exception {
        final StubFilePath p = StubFilePathBuilder.start("testverz")
                .build();

        final String expected = GeneratorCommandBuilder.start()
                .init("/tmp")
                .stepDown("testverz")
                .stepUp()
                .toHexString();

        final String actual = callEnumerator(p, "/tmp");
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
                .init("/tmp/123")
                .stepDown("x")
                .stepDown("a")
                .stepUp()
                .stepDown("b")
                .stepUp()
                .stepUp()
                .toHexString();

        final String actual = callEnumerator(p, "/tmp/123");
        assertEquals(expected, actual);
    }


    @Test
    public void testSendFiles() throws Exception {
        final StubFilePath p = StubFilePathBuilder.start("x")
                .file("a.txt", 42, 123)
                .file("b.txt", 43, 124)
                .build();

        final String expected = GeneratorCommandBuilder.start()
                .init("")
                .stepDown("x")
                .file("a.txt", 42, 123)
                .file("b.txt", 43, 124)
                .stepUp()
                .toHexString();

        final String actual = callEnumerator(p, "");
        assertEquals(expected, actual);
    }

}
