package dev.enola.be.task;

public class StatusTest {

    public static void main(String[] args) {
        testAllStatusValues();
        testIsTerminalForPending();
        testIsTerminalForInProgress();
        testIsTerminalForSuccessful();
        testIsTerminalForFailed();
        testIsTerminalForCancelled();
    }
    
    private static void testAllStatusValues() {
        var values = Status.values();
        assert values.length == 5 : "Expected 5 status values, got " + values.length;
        assert values[0] == Status.PENDING;
        assert values[1] == Status.IN_PROGRESS;
        assert values[2] == Status.SUCCESSFUL;
        assert values[3] == Status.FAILED;
        assert values[4] == Status.CANCELLED;
    }
    
    private static void testIsTerminalForPending() {
        assert !Status.PENDING.isTerminal() : "PENDING should not be terminal";
    }
    
    private static void testIsTerminalForInProgress() {
        assert !Status.IN_PROGRESS.isTerminal() : "IN_PROGRESS should not be terminal";
    }
    
    private static void testIsTerminalForSuccessful() {
        assert Status.SUCCESSFUL.isTerminal() : "SUCCESSFUL should be terminal";
    }
    
    private static void testIsTerminalForFailed() {
        assert Status.FAILED.isTerminal() : "FAILED should be terminal";
    }
    
    private static void testIsTerminalForCancelled() {
        assert Status.CANCELLED.isTerminal() : "CANCELLED should be terminal";
    }
}
