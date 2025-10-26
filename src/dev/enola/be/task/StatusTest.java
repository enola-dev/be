package dev.enola.be.task;

import static ch.vorburger.test.Assert.assertTrue;

public class StatusTest {

    public static void main(String[] args) {
        testIsTerminal();
    }

    private static void testIsTerminal() {
        assertTrue(!Status.PENDING.isTerminal(), "PENDING should not be terminal");
        assertTrue(!Status.IN_PROGRESS.isTerminal(), "IN_PROGRESS should not be terminal");
        assertTrue(Status.COMPLETED.isTerminal(), "COMPLETED should be terminal");
        assertTrue(Status.FAILED.isTerminal(), "FAILED should be terminal");
        assertTrue(Status.CANCELLED.isTerminal(), "CANCELLED should be terminal");
    }
}
