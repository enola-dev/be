package dev.enola.be.task.test;

import dev.enola.be.task.Empty;
import dev.enola.be.task.Task;

public class FailingTask extends Task<Empty, String> {

    public enum FailureMode {
        RUNTIME_EXCEPTION,
        CHECKED_EXCEPTION,
        ERROR,
    }

    private final FailureMode failureMode;

    public FailingTask(FailureMode failureMode) {
        super(Empty.INSTANCE);
        this.failureMode = failureMode;
    }

    public FailingTask() {
        this(FailureMode.RUNTIME_EXCEPTION);
    }

    @Override
    protected String execute() throws Exception {
        switch (failureMode) {
            case RUNTIME_EXCEPTION -> throw new RuntimeException("Intentional runtime exception");
            case CHECKED_EXCEPTION -> throw new Exception("Intentional checked exception");
            case ERROR -> throw new Error("Intentional error exception");
            default -> throw new IllegalStateException("Unknown failure mode: " + failureMode);
        }
    }
}
