package de.tntinteractive.jsync;

public enum SenderCommand {
    FILE_START(1),
    HASH(2),
    FILE_END(3);

    private final byte code;

    SenderCommand(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return this.code;
    }

}
