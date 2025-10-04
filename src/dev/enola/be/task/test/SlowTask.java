package dev.enola.be.task.test;

import dev.enola.be.task.Task;
import dev.enola.common.concurrent.Threads;

import java.time.Duration;

public class SlowTask extends Task<String, String> {

    private final Duration sleep;
    private final Duration timeout;

    public SlowTask(String input, long sleepMillis, Duration timeout) {
        super(input);
        this.sleep = Duration.ofMillis(sleepMillis);
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
        Threads.sleep(sleep);
        return "Completed: " + input;
    }
}
