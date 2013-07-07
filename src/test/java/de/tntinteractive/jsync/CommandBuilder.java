package de.tntinteractive.jsync;

import java.io.ByteArrayOutputStream;

public class CommandBuilder {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    protected ByteArrayOutputStream getBuffer() {
        return this.buffer;
    }

    public byte[] toByteArray() {
        return this.buffer.toByteArray();
    }

    public String toHexString() {
        return TestHelper.toHexString(this.toByteArray());
    }

}
