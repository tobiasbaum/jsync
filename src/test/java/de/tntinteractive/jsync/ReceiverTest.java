package de.tntinteractive.jsync;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

public class ReceiverTest {

    private static void callReceiver(ReceiverCommandBuilder input, BlockingQueue<Integer> toResend, FilePath... files) {
        final FilePathBuffer b = new FilePathBuffer();
        for (final FilePath p : files) {
            b.add(p);
        }
        final ByteArrayInputStream source = new ByteArrayInputStream(input.toByteArray());
        final Receiver e = new Receiver(source, b, toResend);
        e.run();
    }

    @Test
    public void testSimpleReception() throws Exception {
        final ReceiverCommandBuilder input = ReceiverCommandBuilder.start()
                .startFile(0)
                .rawData("dateiinhalt")
                .endFile(TestHelper.md4("dateiinhalt"))
                .enumeratorDone();

        final StubFilePath dir = new StubFilePath(null, "dir");
        final StubFilePath f = new StubFilePath(dir, "datei");

        final BlockingQueue<Integer> toResend = new LinkedBlockingQueue<Integer>();
        callReceiver(input, toResend, f);

        checkDirectoryContent(dir, "datei");
        checkContent(dir.getChild("datei"), "dateiinhalt");
        checkToResend(toResend, -1);
    }

    @Test
    public void testReceptionWithWrongChecksumLeadsToResend() throws Exception {
        final ReceiverCommandBuilder input = ReceiverCommandBuilder.start()
                .startFile(0)
                .rawData("dateiinhalt")
                .endFile(TestHelper.md4("falsch"));

        final StubFilePath dir = new StubFilePath(null, "dir");
        final StubFilePath f = new StubFilePath(dir, "datei", "alter Inhalt");

        final BlockingQueue<Integer> toResend = new LinkedBlockingQueue<Integer>();
        callReceiver(input, toResend, f);

        checkDirectoryContent(dir, "datei");
        checkContent(dir.getChild("datei"), "alter Inhalt");
        checkToResend(toResend, 0, -1);
    }

    @Test
    public void testReceptionWithWrongChecksumLeadsToResendUntilOK() throws Exception {
        final ReceiverCommandBuilder input = ReceiverCommandBuilder.start()
                .startFile(0)
                .rawData("dateiinhalt")
                .endFile(TestHelper.md4("falsch"))
                .enumeratorDone()
                .startFile(0)
                .rawData("dateiinhalt")
                .endFile(TestHelper.md4("auch falsch"))
                .startFile(0)
                .rawData("dateiinhalt")
                .endFile(TestHelper.md4("dateiinhalt"));

        final StubFilePath dir = new StubFilePath(null, "dir");
        final StubFilePath f = new StubFilePath(dir, "datei", "alter Inhalt");

        final BlockingQueue<Integer> toResend = new LinkedBlockingQueue<Integer>();
        callReceiver(input, toResend, f);

        checkDirectoryContent(dir, "datei");
        checkContent(dir.getChild("datei"), "dateiinhalt");
        checkToResend(toResend, 0, 0, -1);
    }

    private static void checkToResend(BlockingQueue<Integer> toResend, Integer... expectedValues) {
        final ArrayList<Integer> actual = new ArrayList<Integer>();
        toResend.drainTo(actual);
        assertEquals(Arrays.asList(expectedValues), actual);
    }

    private static void checkDirectoryContent(StubFilePath dir, String... exceptedNames) throws Exception {
        assertEquals(
                Arrays.asList(exceptedNames),
                TestHelper.getChildrenNames(dir));
    }

    private static void checkContent(StubFilePath file, String expected) {
        assertEquals(
                expected,
                file.getContent());
    }

}
