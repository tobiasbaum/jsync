package de.tntinteractive.jsync;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Test;

public class SenderTest {

    private static String callSender(SenderCommandBuilder input, FilePath... files) throws Exception {
        final FilePathBuffer b = new FilePathBuffer();
        for (final FilePath p : files) {
            b.add(p);
        }
        final ByteArrayInputStream source = new ByteArrayInputStream(input.toByteArray());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ExceptionBuffer exc = new ExceptionBuffer();
        final Sender e = new Sender(source, b, buffer, exc);
        e.run();
        exc.doHandling();
        return TestHelper.toHexString(buffer.toByteArray());
    }

    @Test
    public void testSendWithoutHashes() throws Exception {
        final SenderCommandBuilder input = SenderCommandBuilder.start()
                .startFile(0)
                .endFile()
                .enumeratorDone()
                .everythingOk();

        final String data = "dies sind daten in der datei";
        final StubFilePath file = new StubFilePath(null, "datei", data);

        final String expected = ReceiverCommandBuilder.start()
                .startFile(0)
                .rawData(data)
                .endFile(TestHelper.md4(data))
                .enumeratorDone()
                .toHexString();

        final String actual = callSender(input, file);
        assertEquals(expected, actual);
    }

    @Test
    public void testSendEmptyFile() throws Exception {
        final SenderCommandBuilder input = SenderCommandBuilder.start()
                .startFile(0)
                .endFile()
                .enumeratorDone()
                .everythingOk();

        final String data = "";
        final StubFilePath file = new StubFilePath(null, "datei", data);

        final String expected = ReceiverCommandBuilder.start()
                .startFile(0)
                .endFile(TestHelper.md4(data))
                .enumeratorDone()
                .toHexString();

        final String actual = callSender(input, file);
        assertEquals(expected, actual);
    }

    @Test
    public void testMultipleFilesWithoutHashes() throws Exception {
        final SenderCommandBuilder input = SenderCommandBuilder.start()
                .startFile(0)
                .endFile()
                .startFile(1)
                .endFile()
                .enumeratorDone()
                .everythingOk();

        final String data1 = "dies sind daten in der ersten datei";
        final StubFilePath file1 = new StubFilePath(null, "datei", data1);

        final String data2 = "dies sind daten in der zweiten datei";
        final StubFilePath file2 = new StubFilePath(null, "datei2", data2);

        final String expected = ReceiverCommandBuilder.start()
                .startFile(0)
                .rawData(data1)
                .endFile(TestHelper.md4(data1))
                .startFile(1)
                .rawData(data2)
                .endFile(TestHelper.md4(data2))
                .enumeratorDone()
                .toHexString();

        final String actual = callSender(input, file1, file2);
        assertEquals(expected, actual);
    }

}
