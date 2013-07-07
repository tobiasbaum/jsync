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

    public SenderCommandBuilder startFile(int index) throws IOException {
        this.writer.writeFileStart(index);
        return this;
    }

    public SenderCommandBuilder endFile() throws IOException {
        this.writer.writeFileEnd();
        return this;
    }

}
