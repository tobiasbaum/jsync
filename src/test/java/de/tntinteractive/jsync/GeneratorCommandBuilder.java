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
