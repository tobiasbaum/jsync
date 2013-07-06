package de.tntinteractive.jsync;

import java.io.DataOutputStream;
import java.io.IOException;

class GeneratorCommandWriter {

    private final DataOutputStream output;

    public GeneratorCommandWriter(DataOutputStream output) {
        this.output = output;
    }

    void writeInitInfo(String remoteParentDir) throws IOException {
        this.output.writeByte(GeneratorCommand.INIT.getCode());
        this.output.writeUTF(remoteParentDir);
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

}
