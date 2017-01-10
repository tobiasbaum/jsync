/*
    Copyright (C) 2013-2017  Tobias Baum <tbaum at tntinteractive.de>

    This file is a part of jsync.

    jsync is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jsync is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jsync.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    @SuppressWarnings("unchecked")
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
