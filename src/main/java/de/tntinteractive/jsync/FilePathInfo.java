package de.tntinteractive.jsync;

public class FilePathInfo {
    private final FilePathInfo parent;
    private final String name;
    private final boolean isDir;

    public FilePathInfo(FilePathInfo parent, String name) {
        this.parent = parent;
        this.name = name;
        this.isDir = true;
    }

    public String getName() {
        return this.name;
    }

    public FilePathInfo getParent() {
        return this.parent;
    }

    public boolean isDirectory() {
        return this.isDir;
    }
}