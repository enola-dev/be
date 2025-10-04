package dev.enola.be.task;

/** Exception thrown when a task is awaited with {@link TaskExecutor#await(Task)} and fails. */
public class UncheckedTaskAwaitException extends RuntimeException {

    public UncheckedTaskAwaitException(String message, Throwable cause) {
        super(message, cause);
    }
}
