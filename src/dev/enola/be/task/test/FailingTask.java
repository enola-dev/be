package dev.enola.be.task.test;

import dev.enola.be.task.Task;

public class FailingTask extends Task<String, String> {
    public FailingTask(String input) {
        super(input);
    }

    @Override
    protected String execute() throws Exception {
        throw new RuntimeException("Intentional failure");
    }
}
