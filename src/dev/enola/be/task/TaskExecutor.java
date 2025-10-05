package dev.enola.be.task;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import dev.enola.common.concurrent.Executors;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class TaskExecutor implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(TaskExecutor.class.getName());

    // TODO Synthetic "root" task, to which all running tasks are children?
    // This could be useful for managing task hierarchies and dependencies.

    // This map has a basic time-based eviction policy; see constructor.
    // A more sophisticated implementation could persist completed tasks, so that get()
    // can still find them later, with a separate eviction policy for that persistent store.
    private final Map<UUID, Task<?, ?>> tasks = new ConcurrentHashMap<>();

    // Nota bene: In *THEORY* we should *NEVER* have *ANY* uncaught exceptions from Task,
    // because any exception thrown by the task's `execute()` method would be caught and
    // wrapped in an ExecutionException by the Future returned by ExecutorService.submit().
    //
    // But in practice, who knows what the future holds, so we better log them just in case;
    // just because "swallowed" lost exceptions are seriously the worst kind of bugs to diagnose!
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(LOG);

    private final ScheduledExecutorService timeoutScheduler =
            Executors.newSingleThreadScheduledExecutor("TaskExecutor-Timeout", LOG);

    private final ScheduledExecutorService cleanupScheduler =
            Executors.newSingleThreadScheduledExecutor("TaskExecutor-Cleanup", LOG);

    public TaskExecutor(Duration completedTaskEvictionInterval) {
        if (completedTaskEvictionInterval == null) {
            throw new IllegalArgumentException("completedTaskEvictionInterval must not be null");
        }
        if (completedTaskEvictionInterval.isNegative() || completedTaskEvictionInterval.isZero()) {
            throw new IllegalArgumentException("completedTaskEvictionInterval must be positive");
        }
        var m = completedTaskEvictionInterval.toMillis();
        cleanupScheduler.scheduleAtFixedRate(this::evictCompletedTasks, m, m, MILLISECONDS);
    }

    public TaskExecutor() {
        this(Duration.ofHours(1));
    }

    private void evictCompletedTasks() {
        tasks.values().removeIf(task -> task.status().isTerminal());
    }

    private static class LoggingFutureTask<V> extends FutureTask<V> {
        private final Task<?, V> task;

        public LoggingFutureTask(Callable<V> callable, Task<?, V> task) {
            super(callable);
            this.task = task;
        }

        @Override
        protected void done() {
            task.endedAt(Instant.now());
            LOG.fine(() -> task.toString());
        }
    }

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

            Callable<O> callable = new TaskCallable<>(task);
            var futureTask = new LoggingFutureTask<>(callable, task);

            executor.execute(futureTask);

            var timeout = task.timeout();
            if (!timeout.isZero() && !timeout.isNegative()) {
                timeoutScheduler.schedule(
                        () -> futureTask.cancel(true), timeout.toMillis(), MILLISECONDS);
            }

            task.future(futureTask);
            return futureTask;
        }
    }

    /**
     * Submits a task for execution and waits for it to complete, returning its result.
     *
     * <p>This is a convenience method that basically combines submitting a task via {@link
     * #async(Task)} and then waiting for it to complete via {@link Task#await()}.
     *
     * @param task the task to execute
     * @return the computed result of the task
     * @throws IllegalStateException if the task was already submitted
     * @throws UncheckedTaskAwaitException see {@link Task#await()}
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
        // Signal to all running tasks, so they can terminate gracefully & fast
        for (Task<?, ?> task : tasks.values()) {
            task.cancel();
        }

        cleanupScheduler.close();
        timeoutScheduler.close();
        executor.close();
    }
}
