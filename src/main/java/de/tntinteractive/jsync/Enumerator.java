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
import java.io.OutputStream;

/**
 * Bestimmt alle Dateien und Verzeichnisse in einem Verzeichnisbaum und gibt diese
 * zusammen mit ihren relevanten Eigenschaften an den {@link Generator} weiter.
 * Gibt dem Daemon außerdem Initialisierungsinformationen bekannt.
 */
public class Enumerator implements Runnable {

    private final FilePath localDir;
    private final GeneratorCommandWriter writer;
    private final FastConcurrentList<FilePath> filePaths;
    private final ExceptionBuffer exc;

    public Enumerator(FilePath localDir, OutputStream target,
            FastConcurrentList<FilePath> filePathBuffer, ExceptionBuffer exc) {
        this.localDir = localDir;
        this.writer = new GeneratorCommandWriter(new DataOutputStream(target));
        this.filePaths = filePathBuffer;
        this.exc = exc;
    }

    @Override
    public void run() {
        try {
            final int count = this.sendDirRecursive(this.localDir);
            System.out.println("Enumerated " + count + " files.");
        } catch (final IOException e) {
            this.exc.addThrowable(e);
        } finally {
            this.writer.close();
        }
    }

    private int sendDirRecursive(FilePath dir) throws IOException {
        int count = 0;
        this.writer.writeStepDown(dir.getName());
        for (final FilePath child : dir.getChildrenSorted()) {
            if (child.isDirectory()) {
                count += this.sendDirRecursive(child);
            } else {
                this.filePaths.add(child);
                this.writer.writeFile(child.getName(), child.getSize(), child.getLastChange());
                count++;
            }
        }
        this.writer.writeStepUp();
        return count;
    }

}
