package de.tntinteractive.jsync;

public enum SenderCommand {
    FILE_START(1),
    HASH(2),
    FILE_END(3),
    ENUMERATOR_DONE(4),
    EVERYTHING_OK(5);

    private final byte code;

    SenderCommand(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return this.code;
    }

}
