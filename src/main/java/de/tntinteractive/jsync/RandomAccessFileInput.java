package de.tntinteractive.jsync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

class RandomAccessFileInput implements RandomAccessInput {

    private final FileChannel f;
    private final ByteBuffer buffer;

    public RandomAccessFileInput(File file) throws IOException {
        this.f = new FileInputStream(file).getChannel();
        this.buffer = ByteBuffer.wrap(new byte[Short.MAX_VALUE]);
    }

    @Override
    public void copyTo(OutputStream target, long offset, short length) throws IOException {
        this.buffer.rewind();
        this.buffer.limit(length);
        this.f.position(offset);
        while (this.buffer.position() < this.buffer.limit()) {
            final int read = this.f.read(this.buffer, offset);
            if (read < 0) {
                break;
            }
        }
        target.write(this.buffer.array(), 0, this.buffer.position());
    }

    @Override
    public void close() throws IOException {
        this.f.close();
    }

}
