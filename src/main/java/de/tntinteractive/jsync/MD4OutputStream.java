package de.tntinteractive.jsync;

import java.io.IOException;
import java.io.OutputStream;

public class MD4OutputStream extends OutputStream {

    private final MD4 md4;
    private final OutputStream decorated;

    public MD4OutputStream(OutputStream decorated) {
        this.decorated = decorated;
        this.md4 = new MD4();
    }

    @Override
    public void write(int b) throws IOException {
        this.md4.engineUpdate((byte) b);
        this.decorated.write(b);
    }

    @Override
    public void write(byte[] buffer, int off, int len) throws IOException {
        this.md4.engineUpdate(buffer, off, len);
        this.decorated.write(buffer, off, len);
    }

    @Override
    public void close() throws IOException {
        this.decorated.close();
    }

    public byte[] getDigest() {
        return this.md4.engineDigest();
    }

}
