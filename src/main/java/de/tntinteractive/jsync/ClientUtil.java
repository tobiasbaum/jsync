package de.tntinteractive.jsync;

public class ClientUtil {

    public static void handleException(Throwable t) {
        t.printStackTrace();
        System.exit(99);
    }

}
