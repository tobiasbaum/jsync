package de.tntinteractive.jsync;

import java.io.IOException;
import java.io.InputStream;

public class MD4InputStream extends InputStream {

    private final InputStream decorated;
    private final MD4 md4;

    public MD4InputStream(InputStream decorated) {
        this.decorated = decorated;
        this.md4 = new MD4();
    }

    @Override
    public int read() throws IOException {
        final int r = this.decorated.read();
        if (r >= 0) {
            this.md4.engineUpdate((byte) r);
        }
        return r;
    }

    @Override
    public int read(byte[] buffer, int off, int len) throws IOException {
        final int actual = this.decorated.read(buffer, off, len);
        if (actual > 0) {
            this.md4.engineUpdate(buffer, off, actual);
        }
        return actual;
    }

    @Override
    public void close() throws IOException {
        this.decorated.close();
    }

    public byte[] getDigest() {
        return this.md4.engineDigest();
    }

}
