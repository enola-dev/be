package dev.enola.be.task.demo;

import dev.enola.be.task.Task;
import dev.enola.be.task.TaskExecutor;
import dev.enola.be.task.demo.LongIncrementingTask.Input;
import dev.enola.be.task.demo.LongIncrementingTask.Output;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

public class LongIncrementingTask extends Task<Input, Output> {

    record Input(long max, Duration sleep) {}

    record Output(long result) {}

    private final Consumer<Long> progressConsumer;

    public LongIncrementingTask(Input input, Consumer<Long> progressConsumer) {
        super(input);
        this.progressConsumer = progressConsumer;
    }

    @Override
    protected Output execute() throws Exception {
        for (long i = 0; i < input.max; i++) {
            progressConsumer.accept(i);
            Thread.yield();
            if (Thread.currentThread().isInterrupted())
                throw new InterruptedException("Task was interrupted");
            Threads.sleep(input.sleep);
        }

        // TODO Report % progress

        return new Output(input.max);
    }

    private static void simpleLoop(long max, Duration sleep) throws InterruptedException {
        var start = Instant.now();
        for (long i = 0; i < max; i++) Threads.sleep(sleep);
        var duration = Duration.between(start, Instant.now());
        System.out.println(
                "Looped to "
                        + max
                        + " with "
                        + sleep
                        + " sleep, but without output, in "
                        + duration);
    }

    public static void main(String[] args) throws InterruptedException {
        // Count to max, with 1ms pause between each increment
        var max = 10000;
        var sleep = Duration.ofMillis(0);

        var input = new Input(max, sleep);
        var task = new LongIncrementingTask(input, System.out::println);
        try (var executor = new TaskExecutor()) {
            executor.await(task);
            System.out.println(task);
        }

        simpleLoop(max, sleep);
    }
}
