package de.tntinteractive.jsync;

import java.io.DataOutputStream;
import java.io.IOException;

public class SenderCommandBuilder extends CommandBuilder {

    private final SenderCommandWriter writer;

    private SenderCommandBuilder() {
        this.writer = new SenderCommandWriter(new DataOutputStream(this.getBuffer()));
    }

    public static SenderCommandBuilder start() {
        return new SenderCommandBuilder();
    }

    public SenderCommandBuilder startFile(int index, int strongHashSize, int blockSize) throws IOException {
        this.writer.writeFileStart(index, strongHashSize, blockSize);
        return this;
    }

    public SenderCommandBuilder hash(int rollingChecksum, byte[] shortMD4) throws IOException {
        this.writer.writeHashes(rollingChecksum, shortMD4);
        return this;
    }

    public SenderCommandBuilder endFile() throws IOException {
        this.writer.writeFileEnd();
        return this;
    }

    public SenderCommandBuilder enumeratorDone() throws IOException {
        this.writer.writeEnumeratorDone();
        return this;
    }

    public SenderCommandBuilder everythingOk() throws IOException {
        this.writer.writeEverythingOk();
        return this;
    }

}
