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