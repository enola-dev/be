package dev.enola.be.task;

/**
 * Status of a {@link Task}, obtained via {@link Task#status()}, with fixed <a href=
 * "https://www.mermaidchart.com/d/ef337271-7f85-4a77-8353-9e4a880efe2a">State Transitions</a>.
 */
public enum Status {

    /** Pending means that the {@link Task} has not yet been submitted to a {@link TaskExecutor}. */
    PENDING,

    /**
     * In Progress means that the {@link Task} is currently being actively worked on by a {@link
     * TaskExecutor}. (It may be blocked by waiting on I/O, but is still considered "in progress",
     * until it reaches a terminal state.)
     */
    IN_PROGRESS,

    /**
     * Completed means that the task has reached an end state with a result {@link Task#output()}
     * that is now available. Note that this does not per-se imply a "success", but really just
     * "completion without technical failure" (or cancellation).
     *
     * <p>It is often up to the interpretation of the resulting output what a "successful" result
     * is. For example, a {@link Task} that was supposed to find a record matching certain criteria
     * may complete successfully with an empty result, meaning that no such record exists - if
     * that's the expected outcome, then that is a "successful" result, otherwise not. Similarly, if
     * a non-zero OS process exit code is considered a "successful" Task completion, or indicates an
     * error, is up to interpretation, and completely separate from the Task Status completion or
     * technical failure or cancellation.
     */
    COMPLETED,

    /**
     * Failed means the task has encountered a technical error during {@link Task#execute()}.
     *
     * <p>There is no {@link Task#output()} available.
     */
    FAILED,

    /**
     * Cancelled means the task has been cancelled with {@link Task#cancel()}.
     *
     * <p>It may also mean that the task has timed out, if it was submitted with a timeout.
     *
     * <p>There is no {@link Task#output()} available.
     */
    CANCELLED; // incl. TIMED_OUT

    /**
     * Whether this status is a terminal one, i.e. no further state transitions will occur.
     *
     * <p>Terminal states are: {@link #COMPLETED}, {@link #FAILED}, {@link #CANCELLED}; non-terminal
     * states are: {@link #PENDING}, {@link #IN_PROGRESS}.
     */
    public boolean isTerminal() {
        return switch (this) {
            case COMPLETED, FAILED, CANCELLED -> true;
            case PENDING, IN_PROGRESS -> false;
        };
    }
}
