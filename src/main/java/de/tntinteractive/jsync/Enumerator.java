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
    private final FilePathBuffer filePaths;

    public Enumerator(FilePath localDir, OutputStream target, FilePathBuffer filePathBuffer) {
        this.localDir = localDir;
        this.writer = new GeneratorCommandWriter(new DataOutputStream(target));
        this.filePaths = filePathBuffer;
    }

    @Override
    public void run() {
        try {
            this.sendDirRecursive(this.localDir);
        } catch (final IOException e) {
            ClientUtil.handleException(e);
        } finally {
            this.writer.close();
        }
    }

    private void sendDirRecursive(FilePath dir) throws IOException {
        this.writer.writeStepDown(dir.getName());
        for (final FilePath child : dir.getChildrenSorted()) {
            if (child.isDirectory()) {
                this.sendDirRecursive(child);
            } else {
                this.filePaths.add(child);
                this.writer.writeFile(child.getName(), child.getSize(), child.getLastChange());
            }
        }
        this.writer.writeStepUp();
    }

}
