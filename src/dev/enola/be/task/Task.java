package dev.enola.be.task;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

// TODO ErrorProne @Immutable ?
public abstract class Task<I, O> {

    // TODO Support timeout.. but here, or in TaskExecutor?

    public static <I, O> Task<I, O> create(I input, Function<I, O> function) {
        return new Task<>(input) {
            @Override
            protected O execute() throws Exception {
                return function.apply(input);
            }
        };
    }

    private final UUID id = UUID.randomUUID();
    final AtomicReference<Future<O>> future = new AtomicReference<>();
    protected final I input;

    protected Task(I input) {
        this.input = input;
    }

    protected abstract O execute() throws Exception;

    /** 🆔 */
    public final UUID id() {
        return id;
    }

    public final I input() {
        return input;
    }

    public final Status status() {
        var future = this.future.get();
        if (future == null) return Status.PENDING;
        return switch (future.state()) {
            case RUNNING -> Status.IN_PROGRESS;
            case SUCCESS -> Status.COMPLETED;
            case FAILED -> Status.FAILED;
            case CANCELLED -> Status.CANCELLED;
        };
    }

    // TODO Optional<Instant> startedAt();

    // TODO Optional<Instant> endedAt();

    /** Progress, as 0-100%. */
    // TODO public int progress() {}

    public void cancel() {
        var f = future.get();
        if (f != null) f.cancel(true);
    }

    // TODO Set<Task<?, ?, ?>> dependencies();
}
