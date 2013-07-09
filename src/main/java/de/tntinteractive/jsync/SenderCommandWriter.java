package de.tntinteractive.jsync;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

class SenderCommandWriter {

    private final DataOutputStream output;

    public SenderCommandWriter(DataOutputStream output) {
        this.output = output;
    }

    void writeFileStart(int index, int strongHashSize, int blockSize) throws IOException {
        assert strongHashSize <= Byte.MAX_VALUE;
        assert blockSize <= Short.MAX_VALUE;
        this.output.writeByte(SenderCommand.FILE_START.getCode());
        this.output.writeInt(index);
        this.output.writeByte(strongHashSize);
        this.output.writeShort(blockSize);
    }

    void writeHashes(int rollingHash, byte[] strongHash) throws IOException {
        this.output.writeByte(SenderCommand.HASH.getCode());
        this.output.writeInt(rollingHash);
        this.output.write(strongHash);
    }

    void writeFileEnd() throws IOException {
        this.output.writeByte(SenderCommand.FILE_END.getCode());
    }

    void writeEnumeratorDone() throws IOException {
        this.output.writeByte(SenderCommand.ENUMERATOR_DONE.getCode());
    }

    void writeEverythingOk() throws IOException {
        this.output.writeByte(SenderCommand.EVERYTHING_OK.getCode());
    }

    public void close() {
        try {
            this.output.close();
        } catch (final IOException e) {
            Logger.LOGGER.log(Level.WARNING, "error while closing", e);
        }
    }

}
