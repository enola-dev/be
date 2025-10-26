package ch.vorburger.test;

import java.io.File;

// TODO Replace with Truth once we can depend on it here
public final class Assert {

    public static void assertTrue(boolean check) {
        if (!check) throw new AssertionError();
    }

    public static void assertTrue(boolean check, Object message) {
        if (!check) throw new AssertionError(message);
    }

    public static void assertExists(File file) {
        if (!file.exists()) throw new AssertionError(file + " does not exist");
    }

    private Assert() {}
}
