package dev.enola.be.task.demo;

import java.time.Duration;

import dev.enola.be.task.Task;
import dev.enola.be.task.demo.LongIncrementingTask.Input;
import dev.enola.be.task.demo.LongIncrementingTask.Output;

public class LongIncrementingTask extends Task<Input, Output> {

    record Input(long max, Duration sleep) {
    }

    record Output(long result) {
    }

    protected LongIncrementingTask(Input input) {
        super(input);
    }

    @Override
    protected Output execute() throws Exception {
        for (long i = 0; i < input.max; i++)
            if (Thread.currentThread().isInterrupted())
                throw new InterruptedException("Task was interrupted");
        Thread.sleep(input.sleep.toMillis());

        // TODO Report % progress

        // TODO Stream counting

        return new Output(input.max);
    }
}
