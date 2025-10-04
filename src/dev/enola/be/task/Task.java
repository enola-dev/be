package dev.enola.be.task;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

// TODO ErrorProne @Immutable ?
public abstract class Task<I, O> {

    private final UUID id = UUID.randomUUID();
    final AtomicReference<Future<O>> future = new AtomicReference<>();
    protected final I input;

    protected Task(I input) {
        this.input = requireNonNull(input);
    }

    protected abstract O execute() throws Exception;

    /** 🆔 */
    public final UUID id() {
        return id;
    }

    public final I input() {
        return input;
    }

    public final Optional<O> output() {
        if (status() != Status.COMPLETED) return Optional.empty();
        return Optional.of(future.get().resultNow());
    }

    public final Optional<Throwable> failure() {
        if (status() != Status.FAILED) return Optional.empty();
        return Optional.of(future.get().exceptionNow());
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

    /**
     * Timeout duration, when task should be automatically cancelled.
     *
     * <p>{@link Duration#ZERO} means no (infinite) timeout.
     */
    public Duration timeout() {
        return Duration.ZERO;
    }

    // TODO Set<Task<?, ?, ?>> dependencies();

    public void toString(StringBuilder sb) {
        sb.append("type: Task # ");
        sb.append(getClass().getSimpleName());
        sb.append("\nid: ");
        sb.append(id().toString());
        sb.append("\nstatus: ");
        sb.append(status().toString());

        if (timeout() != Duration.ZERO) {
            sb.append("\ntimeout: ");
            sb.append(timeout().toString());
        }

        var input = input();
        if (input != Empty.INSTANCE) {
            sb.append("\ninput: ");
            sb.append(input.toString()); // TODO Use Jackson?
        }

        output().ifPresent(o -> sb.append("\noutput: ").append(o.toString())); // TODO Use Jackson?
        failure().ifPresent(t -> sb.append("\nfailure: ").append(t.toString()));

        sb.append("\n");
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }
}
