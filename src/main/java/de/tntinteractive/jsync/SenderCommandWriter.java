package de.tntinteractive.jsync;

import java.io.DataOutputStream;
import java.io.IOException;

class SenderCommandWriter {

    private final DataOutputStream output;

    public SenderCommandWriter(DataOutputStream output) {
        this.output = output;
    }

    void writeFileStart(int index) throws IOException {
        this.output.writeByte(SenderCommand.FILE_START.getCode());
        this.output.writeInt(index);
    }

    void writeFileEnd() throws IOException {
        this.output.writeByte(SenderCommand.FILE_END.getCode());
    }

}
