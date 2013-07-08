package de.tntinteractive.jsync;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class ExceptionBuffer {

    private final CopyOnWriteArrayList<Exception> buffer = new CopyOnWriteArrayList<Exception>();

    public void addThrowable(Exception t) {
        Logger.LOGGER.log(Level.SEVERE, "fatal error occured", t);
        this.buffer.add(t);
    }

    public void doHandling() throws Exception {
        if (!this.buffer.isEmpty()) {
            throw this.buffer.get(0);
        }
    }

}
