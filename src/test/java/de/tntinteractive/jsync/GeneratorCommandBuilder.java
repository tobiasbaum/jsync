package de.tntinteractive.jsync;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GeneratorCommandBuilder {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final GeneratorCommandWriter writer = new GeneratorCommandWriter(new DataOutputStream(this.buffer));

    public static GeneratorCommandBuilder start() {
        return new GeneratorCommandBuilder();
    }

    public GeneratorCommandBuilder init(String remoteParentDir) throws IOException {
        this.writer.writeInitInfo(remoteParentDir);
        return this;
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

    public String toHexString() {
        return TestHelper.toHexString(this.buffer.toByteArray());
    }

}
