package de.tntinteractive.jsync;

import java.util.TreeMap;

public class StubFilePath implements FilePath {

    private final StubFilePath parent;
    private final String name;
    private final boolean isDir;
    private final TreeMap<String, StubFilePath> children;
    private final long size;
    private final long lastChange;

    /**
     * Konstruktor für Verzeichnis.
     */
    public StubFilePath(StubFilePath parent, String dirName) {
        this.parent = parent;
        this.name = dirName;
        this.children = new TreeMap<String, StubFilePath>();
        this.isDir = true;
        this.size = 0;
        this.lastChange = 0;

        if (parent != null) {
            parent.children.put(dirName, this);
        }
    }

    /**
     * Konstruktor für Datei.
     */
    public StubFilePath(StubFilePath parent, String filename, long size, long lastChange) {
        this.parent = parent;
        this.name = filename;
        this.children = null;
        this.isDir = false;
        this.size = size;
        this.lastChange = lastChange;

        if (parent != null) {
            parent.children.put(filename, this);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Iterable<? extends FilePath> getChildrenSorted() {
        return this.children.values();
    }

    @Override
    public boolean isDirectory() {
        return this.isDir;
    }

    public StubFilePath getParent() {
        return this.parent;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public long getLastChange() {
        return this.lastChange;
    }

}
