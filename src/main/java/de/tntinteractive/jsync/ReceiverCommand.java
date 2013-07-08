package de.tntinteractive.jsync;

public enum ReceiverCommand {
    FILE_START(1),
    RAW_DATA(2),
    FILE_END(3),
    ENUMERATOR_DONE(4);

    private final byte code;

    ReceiverCommand(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return this.code;
    }

}
