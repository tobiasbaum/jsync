package de.tntinteractive.jsync;

public class StubFilePathBuilder {

    private StubFilePath current;

    private StubFilePathBuilder(StubFilePath stubFilePath) {
        this.current = stubFilePath;
    }

    public static StubFilePathBuilder start(String dirName) {
        return new StubFilePathBuilder(new StubFilePath(null, dirName));
    }

    public StubFilePathBuilder startDir(String dirName) {
        this.current = new StubFilePath(this.current, dirName);
        return this;
    }

    public StubFilePathBuilder endDir() {
        this.current = this.current.getParent();
        return this;
    }

    public StubFilePathBuilder file(String name, long size, long lastChange) {
        new StubFilePath(this.current, name, size, lastChange);
        return this;
    }

    public StubFilePath build() {
        assert this.current.getParent() == null;
        return this.current;
    }

}
