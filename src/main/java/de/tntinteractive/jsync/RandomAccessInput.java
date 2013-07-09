package de.tntinteractive.jsync;

import java.io.IOException;
import java.io.OutputStream;

public interface RandomAccessInput {

    public abstract void copyTo(OutputStream target, long offset, short length) throws IOException;

    public abstract void close() throws IOException;

}
