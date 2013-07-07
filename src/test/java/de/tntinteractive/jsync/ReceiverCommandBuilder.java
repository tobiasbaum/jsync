package de.tntinteractive.jsync;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ReceiverCommandBuilder extends CommandBuilder {

    private final ReceiverCommandWriter writer;

    private ReceiverCommandBuilder() {
        this.writer = new ReceiverCommandWriter(new DataOutputStream(this.getBuffer()));
    }

    public static ReceiverCommandBuilder start() {
        return new ReceiverCommandBuilder();
    }

    public ReceiverCommandBuilder startFile(int index) throws IOException {
        this.writer.writeFileStart(index);
        return this;
    }

    public ReceiverCommandBuilder endFile(byte[] checksum) throws IOException {
        this.writer.writeFileEnd(checksum);
        return this;
    }

    public ReceiverCommandBuilder rawData(String string) throws IOException {
        final byte[] data = TestHelper.toIso(string);
        this.writer.writeRawData(data.length, new ByteArrayInputStream(data));
        return this;
    }

}
