package dev.enola.be.task.demo;

import java.time.Duration;

final class Threads {

    // from
    // https://github.com/enola-dev/enola/blob/main/java/dev/enola/common/concurrent/Threads.java

    /**
     * Sleep 😴 for a certain duration.
     *
     * <p>This is a wrapper around {@link Thread#sleep(Duration)} which (correctly) handles its
     * {@link InterruptedException} by (re-)interrupting the current thread, and then re-throwing it
     * as a checked exception. It also checks for negative duration (which the original method just
     * ignores, which could hide bugs), and has an optimizing shortcut for duration 0.
     *
     * <p>See <a href="https://www.baeldung.com/java-interrupted-exception">Baeldung's related
     * article</a>, or <a
     * href="https://www.yegor256.com/2015/10/20/interrupted-exception.html">yegor256.com Blog
     * Post</a> and <a href="https://github.com/google/guava/issues/1219">Google Guava Issue
     * #1219</a>, as well as Google Guava's Uninterruptibles.sleepUninterruptibly(Duration) (which does
     * something different from this).
     *
     * @param duration Duration to sleep
     * @throws UncheckedInterruptedException if interrupted
     * @throws IllegalArgumentException if duration is negative
     */
    public static void sleep(Duration duration) throws InterruptedException {
        if (duration.isNegative())
            throw new IllegalArgumentException(duration + " cannot be negative");

        if (duration.isZero()) return;

        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // WAS: throw new UncheckedInterruptedException(e);
            throw e;
        }
    }

    private Threads() {}
}
