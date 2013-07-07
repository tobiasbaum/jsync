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

    public abstract FilePath getChild(String string);

    public abstract boolean hasChild(String name);

    public abstract FilePath createSubdirectory(String name);

    public abstract void delete();
}
