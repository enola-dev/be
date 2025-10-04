package dev.enola.be.task;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

public class TaskExecutor implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(TaskExecutor.class.getName());

    // TODO Synthetic "root" task, to which all running tasks are children?
    // This could be useful for managing task hierarchies and dependencies.

    // TODO Eviction policy of completed tasks?! As-is, this leaks memory...
    // E.g. periodically scan the map and remove tasks that are in a terminal state.
    // Persist them first, so that get() can still find them later; with separate
    // eviction policy.
    private final Map<UUID, Task<?, ?>> tasks = new ConcurrentHashMap<>();

    private final ExecutorService executor = TaskExecutorServices.newVirtualThreadPerTaskExecutor();

    // TODO Can the timeoutScheduler also use virtual threads?
    //   (Would need to check if ScheduledExecutorService supports that.)
    //
    // TODO Either way, use LoggingScheduledExecutorService from Enola Commons?
    private final ScheduledExecutorService timeoutScheduler =
            Executors.newSingleThreadScheduledExecutor();

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

        TaskExecutorServices.close(executor);
        TaskExecutorServices.close(timeoutScheduler);
    }
}
