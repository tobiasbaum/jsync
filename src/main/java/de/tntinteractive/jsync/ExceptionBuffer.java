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

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class ExceptionBuffer {

    private final CopyOnWriteArrayList<Exception> buffer = new CopyOnWriteArrayList<Exception>();

    public void addThrowable(Exception t) {
        Logger.LOGGER.log(Level.SEVERE, "fatal error occured", t);
        this.buffer.add(t);
    }

    public void doHandling() throws Exception {
        if (!this.buffer.isEmpty()) {
            throw this.buffer.get(0);
        }
    }

}
