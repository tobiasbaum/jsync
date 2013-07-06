package de.tntinteractive.jsync;

public enum GeneratorCommand {
    INIT(1),
    STEP_DOWN(2),
    FILE(3),
    STEP_UP(4);

    private final byte code;

    GeneratorCommand(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return this.code;
    }

}
