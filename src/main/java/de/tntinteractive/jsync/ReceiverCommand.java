package de.tntinteractive.jsync;

public enum ReceiverCommand {
    FILE_START(1),
    RAW_DATA(2),
    COPY_BLOCK(3),
    FILE_END(4),
    ENUMERATOR_DONE(5);

    private final byte code;

    ReceiverCommand(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return this.code;
    }

}
