package dev.enola.be.task;

/**
 * Unit. Use this instead of {@link Void} (which has to be <code>null</code>) for I or O in {@link
 * Task}.
 */
public final class Empty {

    public static final Empty INSTANCE = new Empty();

    private Empty() {}

    @Override
    public String toString() {
        return "Empty{}";
    }
}
