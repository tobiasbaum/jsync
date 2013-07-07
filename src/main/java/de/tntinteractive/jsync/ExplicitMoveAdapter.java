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
