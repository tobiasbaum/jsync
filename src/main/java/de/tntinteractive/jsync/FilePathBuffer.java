package de.tntinteractive.jsync;

/**
 * Zuordnung der Dateipfade zu Indizes. In der Kommunikation werden überwiegend diese Indizes verwendet.
 * Ist im Endeffekte eine Array-List, aber durch die deutlich verkleinerte Schnittstelle ist thread-safety
 * (mehrere Reader, ein Writer) sehr einfach und performant zu erreichen.
 */
public class FilePathBuffer {

    private int length;
    private volatile FilePath[] content;

    public FilePathBuffer() {
        this.length = 0;
        this.content = new FilePath[128];
    }

    public FilePath get(int index) {
        return this.content[index];
    }

    public int add(FilePath p) {
        final int index = this.length++;
        if (this.length >= this.content.length) {
            final FilePath[] newContent = new FilePath[this.content.length * 2];
            System.arraycopy(this.content, 0, newContent, 0, this.length);
            this.content = newContent;
        }
        this.content[index] = p;
        return index;
    }

}
