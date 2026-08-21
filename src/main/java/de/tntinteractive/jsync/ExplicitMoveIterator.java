package de.tntinteractive.jsync;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

interface ExplicitMoveIterator<T> {
  T get();
  
  void move() throws IOException, ExecutionException, InterruptedException, Exception;
  
  boolean hasCurrent();
}
