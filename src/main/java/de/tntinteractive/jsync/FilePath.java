package de.tntinteractive.jsync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstraktion über Dateisystemiteration (für Testfälle, und um leichter auf die Java 7-Klassen wechseln zu können).
 */
public interface FilePath {

    public abstract String getName();

    public abstract FilePath getParent();

    public abstract Iterable<? extends FilePath> getChildrenSorted() throws IOException;

    public abstract FilePath getChild(String string);

    public abstract boolean hasChild(String name);

    public abstract boolean isDirectory();

    public abstract long getSize();

    public abstract long getLastChange();

    public abstract void setLastChange(long lastChange) throws IOException;

    public abstract FilePath createSubdirectory(String name) throws IOException;

    public abstract void delete() throws IOException;

    public abstract void renameTo(String substring) throws IOException;

    public abstract InputStream openInputStream() throws IOException;

    public abstract OutputStream openOutputStream() throws IOException;

    public abstract RandomAccessInput openRandomAccessInput() throws IOException;

}
