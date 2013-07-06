package de.tntinteractive.jsync;

public class TestHelper {

    public static String toHexString(byte[] byteArray) {
        final StringBuilder ret = new StringBuilder();
        for (final byte b : byteArray) {
            ret.append(toHexString(b));
        }
        return ret.toString();
    }

    private static String toHexString(byte b) {
        return Character.toString(toHexChar(b >> 4))
            + Character.toString(toHexChar(b & 0x0F));
    }

    private static char toHexChar(int nibble) {
        if (nibble < 10) {
            return (char) ('0' + nibble);
        } else {
            return (char) ('A' + nibble - 10);
        }
    }

}
