package de.tntinteractive.jsync;

/**
 * Zuordnung der Dateipfade zu Indizes. In der Kommunikation werden überwiegend diese Indizes verwendet.
 * Ist im Endeffekte eine Array-List, aber durch die deutlich verkleinerte Schnittstelle ist thread-safety
 * (mehrere Reader, ein Writer) sehr einfach und performant zu erreichen.
 */
public class FastConcurrentList<T> {

    private int length;
    private volatile Object[] content;

    public FastConcurrentList() {
        this.length = 0;
        this.content = new Object[128];
    }

    public T get(int index) {
        return (T) this.content[index];
    }

    public int add(T p) {
        final int index = this.length++;
        if (index >= this.content.length) {
            final Object[] newContent = new Object[this.content.length * 2];
            System.arraycopy(this.content, 0, newContent, 0, index);
            this.content = newContent;
        }
        this.content[index] = p;
        return index;
    }

}
