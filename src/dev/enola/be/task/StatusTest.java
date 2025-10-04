package dev.enola.be.task;

public class StatusTest {

    public static void main(String[] args) {
        testIsTerminal();
    }

    private static void testIsTerminal() {
        assert !Status.PENDING.isTerminal() : "PENDING should not be terminal";
        assert !Status.IN_PROGRESS.isTerminal() : "IN_PROGRESS should not be terminal";
        assert Status.SUCCESSFUL.isTerminal() : "SUCCESSFUL should be terminal";
        assert Status.FAILED.isTerminal() : "FAILED should be terminal";
        assert Status.CANCELLED.isTerminal() : "CANCELLED should be terminal";
    }
}
