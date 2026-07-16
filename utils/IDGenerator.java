package utils;

/**
 * Simple utility for generating sequential, prefixed IDs for items and
 * users (e.g. B0001 for books, U0001 for users).
 */
public class IDGenerator {

    private static int itemCounter = 0;
    private static int userCounter = 0;

    public static synchronized String nextItemId(String prefix) {
        itemCounter++;
        return prefix + String.format("%04d", itemCounter);
    }

    public static synchronized String nextUserId() {
        userCounter++;
        return "U" + String.format("%04d", userCounter);
    }

    /** Allows FileHandler to restore counters after loading saved data. */
    public static synchronized void fastForwardItemCounter(int value) {
        if (value > itemCounter) itemCounter = value;
    }

    public static synchronized void fastForwardUserCounter(int value) {
        if (value > userCounter) userCounter = value;
    }
}
