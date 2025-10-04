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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class TaskExecutor implements AutoCloseable {

    // TODO Synthetic "root" task, to which all running tasks are children?
    // This could be useful for managing task hierarchies and dependencies.

    // TODO Eviction policy of completed tasks?! As-is, this leaks memory...
    // E.g. periodically scan the map and remove tasks that are in a terminal state.
    // Persist them first, so that get() can still find them later; with separate
    // eviction policy.
    private final Map<UUID, Task<?, ?>> tasks = new ConcurrentHashMap<>();

    private final ExecutorService executor = TaskExecutorServices.newVirtualThreadPerTaskExecutor();

    private <O> Future<O> future(Task<?, O> task) throws IllegalStateException {
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
            Callable<O> callable = new TaskCallable<>(task);
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
            task.future(future);
            return future;
        }
    }

    /**
     * Submits a task for execution and waits for it to complete, returning its result.
     *
     * <p>This is a convenience method that basically combines submitting a task via {@link
     * #async(Task)} and then waiting for it to complete via {@link Task#await()}. (It's internally
     * implemented almost like that, but not quite; it uses a [small] short-cut for optimization.)
     *
     * @param task the task to execute
     * @return the computed result of the task
     * @throws IllegalStateException if the task was already submitted
     */
    public <O> O await(Task<?, O> task) throws IllegalStateException, UncheckedTaskAwaitException {
        Future<O> future = future(task);
        return task.await(future);
    }

    /**
     * Submits a task for asynchronous execution.
     *
     * <p>The task's {@link Task#execute()} method will be called in a virtual thread. You can later
     * await its completion and get its result with {@link Task#await()} (blocking), or you could
     * use {@link Task#status()}, {@link Task#output()} and {@link Task#failure()} to occasionally
     * inspect the result without blocking.
     *
     * @param task the task to execute
     */
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
        TaskExecutorServices.close(executor);
    }
}
