package de.tntinteractive.jsync;

import java.io.IOException;

interface ExplicitMoveIterator<T> {
    /**
     * Liefert das aktuelle Element.
     * Vor dem ersten move-Aufruf wird das erste Element geliefert (sofern es eins gibt).
     */
    public abstract T get();

    /**
     * Bestimmt das nächste Element und speichert es für die Rückgabe durch get.
     */
    public abstract void move() throws IOException;

    /**
     * Liefert true gdw es ein aktuelles Element gibt (d.h. die Iteration nicht am Ende ist).
     */
    public abstract boolean hasCurrent();
}