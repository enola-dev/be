package dev.enola.be.task.test;

import dev.enola.be.task.Task;

import java.time.Duration;

public class SlowTask extends Task<String, String> {

    private final long sleepMillis;
    private final Duration timeout;

    public SlowTask(String input, long sleepMillis, Duration timeout) {
        super(input);
        this.sleepMillis = sleepMillis;
        this.timeout = timeout;
    }

    public SlowTask(String input, long sleepMillis) {
        this(input, sleepMillis, Duration.ZERO);
    }

    @Override
    public Duration timeout() {
        return timeout;
    }

    @Override
    protected String execute() throws Exception {
        try {
            Thread.sleep(sleepMillis);
            return "Completed: " + input;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }
}
