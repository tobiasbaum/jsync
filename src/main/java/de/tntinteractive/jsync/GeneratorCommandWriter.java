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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

class GeneratorCommandWriter {

    private final DataOutputStream output;

    public GeneratorCommandWriter(DataOutputStream output) {
        this.output = output;
    }

    void writeStepDown(String name) throws IOException {
        this.output.writeByte(GeneratorCommand.STEP_DOWN.getCode());
        this.output.writeUTF(name);
    }

    void writeFile(String name, long size, long lastChange) throws IOException {
        this.output.writeByte(GeneratorCommand.FILE.getCode());
        this.output.writeUTF(name);
        this.output.writeLong(size);
        this.output.writeLong(lastChange);
    }

    void writeStepUp() throws IOException {
        this.output.writeByte(GeneratorCommand.STEP_UP.getCode());
    }

    public void close() {
        try {
            this.output.close();
        } catch (final IOException e) {
            Logger.LOGGER.log(Level.WARNING, "error while closing", e);
        }
    }

}
