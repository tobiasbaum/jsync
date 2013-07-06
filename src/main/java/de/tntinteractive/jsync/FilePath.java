package de.tntinteractive.jsync;

/**
 * Abstraktion über Dateisystemiteration (für Testfälle, und um leichter auf die Java 7-Klassen wechseln zu können).
 */
public interface FilePath {

    public abstract String getName();

    public abstract Iterable<? extends FilePath> getChildrenSorted();

    public abstract boolean isDirectory();

    public abstract long getSize();

    public abstract long getLastChange();

}
