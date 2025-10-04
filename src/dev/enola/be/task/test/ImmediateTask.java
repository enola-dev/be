package dev.enola.be.task.test;

import dev.enola.be.task.Task;

public class ImmediateTask extends Task<String, String> {
    public ImmediateTask(String input) {
        super(input);
    }

    @Override
    protected String execute() throws Exception {
        return "Result: " + input;
    }
}
