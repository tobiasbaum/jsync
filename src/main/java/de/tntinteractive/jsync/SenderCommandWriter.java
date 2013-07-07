package de.tntinteractive.jsync;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

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

    public void close() {
        try {
            this.output.close();
        } catch (final IOException e) {
            Logger.LOGGER.log(Level.WARNING, "error while closing", e);
        }
    }

}
