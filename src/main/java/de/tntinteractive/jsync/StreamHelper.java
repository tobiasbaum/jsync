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
            target.write(buffer, 0, actual);
            remaining -= actual;
        }
    }

}
