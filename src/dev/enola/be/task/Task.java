package dev.enola.be.task;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface Task<ID, I, O> {
    // TODO extends Identifiable<TaskID>

    /** 🆔 */
    ID id();

    I input();

    CompletableFuture<O> output();

    Status status();

    void run() throws Exception;

    Optional<Instant> startedAt();

    Optional<Instant> endedAt();

    /** Progress, as 0-100%. */
    int progress();

    void cancel();

    Set<Task<?, ?, ?>> dependencies();
}
