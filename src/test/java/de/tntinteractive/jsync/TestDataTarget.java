package de.tntinteractive.jsync;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class TestDataTarget extends StreamDataTarget {

    private OutputStream output;

    public TestDataTarget(OutputStream output) {
        this.output = output;
    }

    @Override
    protected OutputStream getStream() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        return this.output;
    }
}
