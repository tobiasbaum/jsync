package de.tntinteractive.jsync;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class ReceiverCommandWriter {

    private final DataOutputStream output;

    public ReceiverCommandWriter(DataOutputStream target) {
        this.output = target;
    }

    public void writeFileStart(int index) throws IOException {
        this.output.writeByte(ReceiverCommand.FILE_START.getCode());
        this.output.writeInt(index);
    }

    public void writeFileEnd(byte[] hash) throws IOException {
        this.output.writeByte(ReceiverCommand.FILE_END.getCode());
        this.output.write(hash);
    }

    public void writeRawData(int length, InputStream data) throws IOException {
        this.output.writeByte(ReceiverCommand.RAW_DATA.getCode());
        this.output.writeInt(length);
        StreamHelper.copy(data, this.output, length);
    }

    public void writeCopyBlock(long startOffset, short length) throws IOException {
        this.output.writeByte(ReceiverCommand.COPY_BLOCK.getCode());
        this.output.writeLong(startOffset);
        this.output.writeShort(length);
    }

    public void writeEnumeratorDone() throws IOException {
        this.output.writeByte(ReceiverCommand.ENUMERATOR_DONE.getCode());
    }

    public void close() {
        try {
            this.output.close();
        } catch (final IOException e) {
            Logger.LOGGER.log(Level.WARNING, "error while closing", e);
        }
    }

}
