package dev.enola.be.exec;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import dev.enola.be.exec.ExecTask.Input;
import dev.enola.be.exec.ExecTask.Output;
import dev.enola.be.task.Task;

public class ExecTask extends Task<Input, Output> {

    record Input(Path cmd, List<String> args, Path cwd, Map<String, String> env) {
    }

    // TODO Replace String with... Stream?!
    record Output(int exitCode, String stdout, String stderr) {
    }

    protected ExecTask(Input input) {
        super(input);
    }

    @Override
    protected Output execute() throws Exception {
        throw new UnsupportedOperationException("TODO");
    }
}
