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
