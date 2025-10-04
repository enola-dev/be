package dev.enola.be.task;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TaskExecutor implements AutoCloseable {

    // TODO Synthetic "root" task, to which all running tasks are children?
    // This could be useful for managing task hierarchies and dependencies.

    private static final long CLOSE_EXECUTOR_SHUTDOWN_AWAIT_SECONDS = 7;

    // TODO Eviction policy of completed tasks?!
    // E.g. periodically scan the map and remove tasks that are in a terminal state.
    // Persist them first, so that get() can still find them later; with separate
    // eviction policy.
    private final Map<UUID, Task<?, ?>> tasks = new ConcurrentHashMap<>();

    // TODO Use dev.enola.common.concurrent; with logging, etc.
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    // TODO @VisibleForTesting // Intentionally only package-private, for now
    <O> Future<O> future(Task<?, O> task) throws IllegalStateException {
        if (tasks.putIfAbsent(task.id(), task) != null)
            throw new IllegalStateException("Task already submitted: " + task.id());

        // This serves to detect if it was already submitted to ANOTHER TaskExecutor
        if (task.status() != Status.PENDING)
            throw new IllegalStateException("Task " + task.id() + " not PENDING: " + task.status());

        Future<O> future;
        var timeout = task.timeout();
        if (timeout.isZero()) {
            future = executor.submit(task::execute);
        } else {
            Collection<? extends Callable<O>> callables = List.of(task::execute);
            try {
                var futures = executor.invokeAll(callables, timeout.toMillis(), MILLISECONDS);
                future = futures.iterator().next();
            } catch (InterruptedException e) {
                future = CompletableFuture.failedFuture(e);
                Thread.currentThread().interrupt();
            }
        }
        task.future.set(future);
        return future;
    }

    public <O> O await(Task<?, O> task) throws IllegalStateException, UncheckedTaskAwaitException {
        Future<O> future = future(task);
        try {
            return future.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedTaskAwaitException("Task canceled (interrupted)", e);

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new UncheckedTaskAwaitException(
                    "Task execution failed: " + cause.getMessage(), cause);
        }
    }

    // TODO Re-think this, if needed; as-is, you would lose the Exception in case of failure!
    //   public void submit(Task<?, ?> task) { future(task); }

    public Task<?, ?> get(UUID id) throws IllegalArgumentException {
        var task = tasks.get(id);
        if (task == null) throw new IllegalArgumentException("No such task: " + id);
        return task;
    }

    public Set<UUID> list() {
        return tasks.keySet();
    }

    @Override
    public void close() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(CLOSE_EXECUTOR_SHUTDOWN_AWAIT_SECONDS, TimeUnit.SECONDS))
            executor.shutdownNow();
    }
}
