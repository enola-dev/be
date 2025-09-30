package dev.enola.be.exec;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import dev.enola.be.exec.ExecTask.Input;
import dev.enola.be.exec.ExecTask.Output;
import dev.enola.be.task.Task;

public class ExecTask extends Task<Input, Output> {

    // TODO Guava dep: ImmutableList<String> args, ImmutableMap<String, String> env
    record Input(Path cmd, List<String> args, @Nullable Path cwd, @Nullable Map<String, String> env) {
        public Input {
            args = List.copyOf(args);
            if (env != null) {
                env = Map.copyOf(env);
            }
        }
    }

    // TODO Replace String with... Stream, or enola.dev.io.Resource, File/Path.
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
