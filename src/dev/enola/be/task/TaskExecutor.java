package dev.enola.be.task;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TaskExecutor implements AutoCloseable {

    // TODO Support submit with timeout.. but here, or in Task?

    // TODO Synthetic "root" task, to which all running tasks are children?
    // This could be useful for managing task hierarchies and dependencies.

    // TODO Eviction policy of completed tasks?!
    // E.g. periodically scan the map and remove tasks that are in a terminal state.
    // Persist them first, so that get() can still find them later; with separate
    // eviction policy.
    private final Map<UUID, Task<?, ?>> tasks = new ConcurrentHashMap<>();

    // TODO Use dev.enola.common.concurrent; with logging, etc.
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    // TODO @VisibleForTesting // Intentionally only package-private, for now
    <O> Future<O> future(Task<?, O> task) {
        if (tasks.putIfAbsent(task.id(), task) != null)
            throw new IllegalStateException("Task already submitted: " + task.id());

        // This serves to detect if it was already submitted to ANOTHER TaskExecutor
        if (task.status() != Status.PENDING)
            throw new IllegalStateException("Task " + task.id() + " not PENDING: " + task.status());

        Future<O> future = executor.submit(task::execute);
        task.future.set(future);
        return future;
    }

    // TODO Should NOT throws Exception
    public <O> O await(Task<?, O> task) throws Exception {
        Future<O> future = future(task);
        return future.get();
    }

    // TODO Should NOT throws Exception
    public void submit(Task<?, ?> task) throws Exception {
        future(task);
    }

    public Task<?, ?> get(UUID id) {
        var task = tasks.get(id);
        if (task == null) throw new IllegalArgumentException("No such task: " + id);
        return task;
    }

    public Set<UUID> list() {
        return tasks.keySet();
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        if (!executor.awaitTermination(7, TimeUnit.SECONDS)) executor.shutdownNow();
    }
}
