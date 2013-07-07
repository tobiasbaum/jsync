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
    private final String remoteParentDir;
    private final GeneratorCommandWriter output;
    private final FilePathBuffer filePaths;

    public Enumerator(FilePath localDir, String remoteParentDir, OutputStream target, FilePathBuffer filePathBuffer) {
        this.localDir = localDir;
        this.remoteParentDir = remoteParentDir;
        this.output = new GeneratorCommandWriter(new DataOutputStream(target));
        this.filePaths = filePathBuffer;
    }

    @Override
    public void run() {
        try {
            this.output.writeInitInfo(this.remoteParentDir);
            this.sendDirRecursive(this.localDir);
        } catch (final IOException e) {
            ClientUtil.handleException(e);
        }
    }

    private void sendDirRecursive(FilePath dir) throws IOException {
        this.output.writeStepDown(dir.getName());
        for (final FilePath child : dir.getChildrenSorted()) {
            if (child.isDirectory()) {
                this.sendDirRecursive(child);
            } else {
                this.filePaths.add(child);
                this.output.writeFile(child.getName(), child.getSize(), child.getLastChange());
            }
        }
        this.output.writeStepUp();
    }

}
