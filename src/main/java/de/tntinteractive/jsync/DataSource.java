package de.tntinteractive.jsync;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface DataSource {
  CommandReader readCommand() throws IOException, ExecutionException, InterruptedException, TimeoutException;
}
