package de.tntinteractive.jsync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class FilePathBufferTest {

    @Test
    public void testFilePathBuffer() {
        final FilePathBuffer b = new FilePathBuffer();
        for (int i = 0; i < 500; i++) {
            final StubFilePath fp = new StubFilePath(null, "d" + i);
            final int idx = b.add(fp);
            assertEquals(i, idx);
            assertSame(fp, b.get(i));
        }
        for (int i = 0; i < 500; i++) {
            assertEquals("d" + i, b.get(i).getName());
        }
    }

}
