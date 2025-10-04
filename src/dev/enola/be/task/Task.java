package dev.enola.be.task;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

// TODO ErrorProne @Immutable ?
public abstract class Task<I, O> {

    private final UUID id = UUID.randomUUID();
    private final AtomicReference<Future<O>> future = new AtomicReference<>();
    private volatile /*TODO @Nullable*/ Instant startedAt;
    private volatile /*TODO @Nullable*/ Instant endedAt;
    protected final I input;

    protected Task(I input) {
        this.input = requireNonNull(input);
    }

    /**
     * The main logic of the task.
     *
     * @return output, never null (use {@link Empty#INSTANCE}; or {@link Optional}, if needed)
     * @throws Exception in case of any failure
     */
    protected abstract O execute() throws Exception;

    // package-private, for TaskExecutor (only)
    final void future(Future<O> future) {
        if (!this.future.compareAndSet(null, future))
            throw new IllegalStateException("Future already set for task " + id());
        this.startedAt = Instant.now();
    }

    /** 🆔 */
    public final UUID id() {
        return id;
    }

    public final I input() {
        return input;
    }

    /**
     * Awaits the completion of the task (as in {@link Status#COMPLETED}), and returns its output.
     *
     * <p>This method can only be called after the task has been submitted to {@link
     * TaskExecutor#async(Task)}. If you need the result immediately after submission anyway,
     * consider just directly using {@link TaskExecutor#await(Task)} instead. (This method however
     * is typically used if you want to first submit a task, do something else, and then later await
     * its result.)
     *
     * <p>Note that any checked exceptions thrown by the task's {@link Task#execute()} method will
     * be wrapped in an {@link UncheckedTaskAwaitException}. {@link RuntimeException} and {@link
     * Error} will be re-thrown as-is.
     *
     * @return the computed result
     * @throws UncheckedTaskAwaitException if the task was cancelled, interrupted, or failed with an
     *     checked exception (wrapped cause). The cause can be inspected to determine the root
     *     cause. If the task fails with a {@link RuntimeException} or {@link Error}, it will be
     *     re-thrown as-is and not wrapped.
     * @throws IllegalStateException if the task was not yet submitted with {@link
     *     TaskExecutor#async(Task)}
     */
    public O await() throws IllegalStateException, UncheckedTaskAwaitException {
        var future = this.future.get();
        if (future == null) {
            throw new IllegalStateException(
                    "Task not yet submitted to TaskExecutor.async: " + id());
        }
        return await(future);
    }

    // package-private, for TaskExecutor (only)
    O await(Future<O> future) throws UncheckedTaskAwaitException {
        try {
            return future.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedTaskAwaitException("Task interrupted execution", e);

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                throw new UncheckedTaskAwaitException("Task execution interrupted", cause);
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new UncheckedTaskAwaitException(
                    "Task execution failed: " + cause.getMessage(), cause);

        } catch (CancellationException e) {
            throw new UncheckedTaskAwaitException("Task cancelled", e);
        }
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

    public final Optional<Instant> startedAt() {
        return Optional.ofNullable(startedAt);
    }

    public final Optional<Instant> endedAt() {
        return Optional.ofNullable(endedAt);
    }

    /* package-private */ final void endedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public final Duration duration() {
        if (startedAt == null) return Duration.ZERO;
        if (endedAt == null) return Duration.between(startedAt, Instant.now());
        return Duration.between(startedAt, endedAt);
    }

    /** Progress, as 0-100%. */
    // TODO public int progress() {}

    public final void cancel() {
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
        sb.append(getClass().getName());

        sb.append("\nid: ");
        sb.append(id().toString());

        sb.append("\nstatus: ");
        sb.append(status().toString());

        startedAt().ifPresent(s -> sb.append("\nstartedAt: ").append(s.toString()));
        endedAt().ifPresent(e -> sb.append("\nendedAt: ").append(e.toString()));

        sb.append("\nduration: ");
        sb.append(duration().toString());

        if (!timeout().isZero()) {
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
    public final String toString() {
        var sb = new StringBuilder();
        toString(sb);
        return sb.toString();
    }
}
