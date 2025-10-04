package dev.enola.be.task;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TaskExecutor implements AutoCloseable {

    // TODO Synthetic "root" task, to which all running tasks are children?
    // This could be useful for managing task hierarchies and dependencies.

    private static final long CLOSE_EXECUTOR_SHUTDOWN_AWAIT_SECONDS = 7;

    // TODO Eviction policy of completed tasks?!
    // E.g. periodically scan the map and remove tasks that are in a terminal state.
    // Persist them first, so that get() can still find them later; with separate
    // eviction policy.
    private final Map<UUID, Task<?, ?>> tasks = new ConcurrentHashMap<>();

    private final ExecutorService executor = TaskExecutorServices.newVirtualThreadPerTaskExecutor();

    // TODO private? Or @VisibleForTesting // Intentionally only package-private, for now
    <O> Future<O> future(Task<?, O> task) throws IllegalStateException {
        if (tasks.putIfAbsent(task.id(), task) != null)
            throw new IllegalStateException("Task already submitted: " + task.id());

        // This serves to detect if it was already submitted to ANOTHER TaskExecutor;
        // synchronizing on the task object ensures that checking its status and submitting it
        // is an atomic operation across all threads and executors.
        synchronized (task) {
            if (task.status() != Status.PENDING)
                throw new IllegalStateException(
                        "Task " + task.id() + " not PENDING: " + task.status());

            Future<O> future;
            var timeout = task.timeout();

            Callable<O> callable =
                    () -> {
                        var thread = Thread.currentThread();
                        var originalThreadName = thread.getName();
                        thread.setName(task.id().toString());
                        try {
                            return task.execute();
                        } finally {
                            thread.setName(originalThreadName);
                        }
                    };

            if (timeout.isZero() || timeout.isNegative()) {
                future = executor.submit(callable);
            } else {
                Collection<? extends Callable<O>> callables = List.of(callable);
                try {
                    // Nota bene: The risk of toMillis() throwing an ArithmeticException is
                    // unrealistically low, as that would require a timeout of more than ~292
                    // million years... :-) But if that ever happens, we want to know about it,
                    // hence no try/catch here.
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
    }

    /**
     * Submits a task for execution and waits for it to complete, returning its result.
     *
     * <p>This is a convenience method that combines submitting a task via {@link #future(Task)} and
     * then waiting for its completion via {@link java.util.concurrent.Future#get()}.
     *
     * <p>Note that any checked exceptions thrown by the task's {@link Task#execute()} method will
     * be wrapped in an {@link UncheckedTaskAwaitException}. {@link RuntimeException} and {@link
     * Error} will be re-thrown as-is.
     *
     * @param task the task to execute
     * @param <O> the type of the task's output
     * @return the computed result
     * @throws IllegalStateException if the task was already submitted
     * @throws UncheckedTaskAwaitException if the task was cancelled, interrupted, or failed with an
     *     checked exception (wrapped cause). The cause can be inspected to determine the root
     *     cause. If the task fails with a {@link RuntimeException} or {@link Error}, it will be
     *     re-thrown as-is and not wrapped.
     */
    public <O> O await(Task<?, O> task) throws IllegalStateException, UncheckedTaskAwaitException {
        Future<O> future = future(task);
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

    public void async(Task<?, ?> task) {
        future(task);
    }

    public Task<?, ?> get(UUID id) throws IllegalArgumentException {
        var task = tasks.get(id);
        if (task == null) throw new IllegalArgumentException("No such task: " + id);
        return task;
    }

    public Set<UUID> list() {
        return tasks.keySet();
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            // Wait for existing tasks to terminate
            if (!executor.awaitTermination(CLOSE_EXECUTOR_SHUTDOWN_AWAIT_SECONDS, SECONDS)) {

                // Cancel currently executing tasks
                executor.shutdownNow();
            }

        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
