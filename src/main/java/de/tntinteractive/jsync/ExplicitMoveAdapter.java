/*
    Copyright (C) 2013  Tobias Baum <tbaum at tntinteractive.de>

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

import java.util.Iterator;

public class ExplicitMoveAdapter<T> implements ExplicitMoveIterator<T> {

    private final Iterator<? extends T> iter;
    private T current;

    public ExplicitMoveAdapter(Iterable<? extends T> iterable) {
        this.iter = iterable.iterator();
        this.move();
    }

    @Override
    public T get() {
        return this.current;
    }

    @Override
    public void move() {
        if (this.iter.hasNext()) {
            this.current = this.iter.next();
        } else {
            this.current = null;
        }
    }

    @Override
    public boolean hasCurrent() {
        return this.current != null;
    }

}
