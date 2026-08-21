package de.tntinteractive.jsync;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class TestDataSource extends StreamDataSource {
    private InputStream input;

    public TestDataSource(InputStream input) {
        this.input = input;
    }

    @Override
    protected InputStream getStream() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        return this.input;
    }
}
