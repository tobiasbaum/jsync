package de.tntinteractive.jsync;

class TargetFileInfo {

    private final FilePath filePath;
    private final long sourceChangeTime;

    public TargetFileInfo(FilePath filePath, long sourceChangeTime) {
        this.filePath = filePath;
        this.sourceChangeTime = sourceChangeTime;
    }

    public FilePath getFilePath() {
        return this.filePath;
    }

    public long getSourceChangeTime() {
        return this.sourceChangeTime;
    }

}
