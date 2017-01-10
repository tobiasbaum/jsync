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

import java.io.DataOutputStream;
import java.io.IOException;

public class GeneratorCommandBuilder extends CommandBuilder {

    private final GeneratorCommandWriter writer;

    public GeneratorCommandBuilder() {
        this.writer = new GeneratorCommandWriter(new DataOutputStream(this.getBuffer()));
    }

    public static GeneratorCommandBuilder start() {
        return new GeneratorCommandBuilder();
    }

    public GeneratorCommandBuilder stepDown(String name) throws IOException {
        this.writer.writeStepDown(name);
        return this;
    }

    public GeneratorCommandBuilder file(String name, long size, long lastChange) throws IOException {
        this.writer.writeFile(name, size, lastChange);
        return this;
    }

    public GeneratorCommandBuilder stepUp() throws IOException {
        this.writer.writeStepUp();
        return this;
    }

}
