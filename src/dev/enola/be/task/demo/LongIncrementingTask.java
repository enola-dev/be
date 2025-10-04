package dev.enola.be.task.demo;

import dev.enola.be.io.LineWriters;
import dev.enola.be.io.NonBlockingLineWriter;
import dev.enola.be.task.Task;
import dev.enola.be.task.TaskExecutor;
import dev.enola.be.task.demo.LongIncrementingTask.Input;
import dev.enola.be.task.demo.LongIncrementingTask.Output;
import dev.enola.common.function.CheckedConsumer;
import dev.enola.common.log.JulConfigurer;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class LongIncrementingTask extends Task<Input, Output> {

    record Input(long max, Duration sleep) {}

    record Output(long result) {}

    private final CheckedConsumer<Long, IOException> progressConsumer;

    public LongIncrementingTask(Input input, CheckedConsumer<Long, IOException> progressConsumer) {
        super(input);
        this.progressConsumer = progressConsumer;
    }

    @Override
    protected Output execute() throws Exception {
        for (long i = 0; i < input.max; i++) {
            progressConsumer.accept(i);
            // Do *NOT* Thread.yield(); that makes it really horribly slow, by like a factor x100!
            if (Thread.currentThread().isInterrupted())
                throw new InterruptedException("Task was interrupted");
            Threads.sleep(input.sleep);
        }

        // TODO Report % progress

        return new Output(input.max);
    }

    private static void simpleLoop(long max, Duration sleep) throws InterruptedException {
        var start = Instant.now();
        for (long i = 0; i < max; i++) {
            // Do *NOT* Thread.yield(); that makes it really horribly slow, by like a factor x100!
            Threads.sleep(sleep);
        }
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
        JulConfigurer.configureRootLogger();

        // Count to max, with sleep pause between each increment
        var max = 1000000L;
        var sleep = Duration.ofMillis(0);

        var input = new Input(max, sleep);
        var pumperTask = new NonBlockingLineWriter(13, LineWriters.SYSTEM_OUT);
        var printingTask = new LongIncrementingTask(input, pumperTask::println);
        var silentTask = new LongIncrementingTask(input, LineWriters.NOOP::println);

        try (var executor = new TaskExecutor()) {
            executor.async(pumperTask);
            executor.async(silentTask);

            executor.await(printingTask);
            silentTask.await();

            simpleLoop(max, sleep);
        }
    }
}
