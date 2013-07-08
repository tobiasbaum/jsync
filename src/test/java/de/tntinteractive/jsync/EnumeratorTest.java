package de.tntinteractive.jsync;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;

import org.junit.Test;


public class EnumeratorTest {

    private static String callEnumerator(StubFilePath p) throws Exception {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ExceptionBuffer exc = new ExceptionBuffer();
        final Enumerator e = new Enumerator(p, buffer, new FilePathBuffer(), exc);
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
