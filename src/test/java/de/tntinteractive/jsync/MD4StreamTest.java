package de.tntinteractive.jsync;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

public class MD4StreamTest {

    @Test
    public void testReadBytes() throws Exception {
        final byte[] input = TestHelper.toIso("dies ist ein test");
        final MD4StreamFilter md4 = new MD4StreamFilter(new ByteArrayInputStream(input));
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
        final MD4StreamFilter md4 = new MD4StreamFilter(new ByteArrayInputStream(input));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        StreamHelper.copy(md4, output, input.length);

        assertEquals(
                TestHelper.toHexString(input),
                TestHelper.toHexString(output.toByteArray()));

        assertEquals(
                TestHelper.toHexString(TestHelper.md4("dies ist ein test")),
                TestHelper.toHexString(md4.getDigest()));
    }

}
