package dev.enola.be.task.demo;

import dev.enola.be.task.Task;
import dev.enola.be.task.demo.LongIncrementingTask.Input;
import dev.enola.be.task.demo.LongIncrementingTask.Output;

import java.time.Duration;
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
            try {
                Thread.sleep(input.sleep.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedException("Task was interrupted during sleep");
            }
        }

        // TODO Report % progress

        // TODO Stream counting

        return new Output(input.max);
    }

    public static void main(String[] args) {
        // Count to 10000, with 1ms pause between each increment
        var input = new Input(10000, Duration.ofMillis(1));
        var task = new LongIncrementingTask(input, System.out::println);
        try (var executor = new dev.enola.be.task.TaskExecutor()) {
            var output = executor.await(task);
            System.out.println(task + " output: " + output);
        }
    }
}
