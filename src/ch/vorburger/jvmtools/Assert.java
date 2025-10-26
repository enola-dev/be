package ch.vorburger.jvmtools;

import java.io.File;

// TODO Replace with Truth once we can depend on it here
final class Assert {

    static void assertTrue(boolean check) {
        if (!check) throw new AssertionError();
    }

    static void assertTrue(boolean check, Object message) {
        if (!check) throw new AssertionError(message);
    }

    static void assertExists(File file) {
        if (!file.exists()) throw new AssertionError(file + " does not exist");
    }

    private Assert() {}
}
