package dev.enola.be.task.test;

import dev.enola.be.task.Task;

public class SlowTask extends Task<String, String> {
    private final long sleepMillis;

    public SlowTask(String input, long sleepMillis) {
        super(input);
        this.sleepMillis = sleepMillis;
    }

    @Override
    protected String execute() throws Exception {
        Thread.sleep(sleepMillis);
        return "Completed: " + input;
    }
}
