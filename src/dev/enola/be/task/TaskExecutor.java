package dev.enola.be.task;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.Nullable;

public class TaskExecutor implements AutoCloseable {

    // TODO Synthetic "root" task, to which all running tasks are children?
    // This could be useful for managing task hierarchies and dependencies.

    // TODO Eviction policy of completed tasks?!
    // E.g. periodically scan the map and remove tasks that are in a terminal state.
    // Persist them first, so that get() can still find them later; with separate
    // eviction policy.
    private final Map<UUID, Task<?, ?>> tasks = new ConcurrentHashMap<>();

    // TODO Use dev.enola.common.concurrent; with logging, etc.
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public <O> Future<O> submit(Task<?, O> task) {
        tasks.put(task.id(), task);
        Future<O> future = executor.submit(task::execute);
        task.future.set(future);
        return future;
    }

    public Task<?, ?> get(UUID id) {
        var task = tasks.get(id);
        if (task == null)
            throw new IllegalArgumentException("No such task: " + id);
        return task;
    }

    public Set<UUID> list() {
        return tasks.keySet();
    }

    @Override
    public void close() throws Exception {
        executor.shutdown();
        if (!executor.awaitTermination(7, TimeUnit.SECONDS))
            executor.shutdownNow();
    }
}
