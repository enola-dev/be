package ch.vorburger.main;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// https://github.com/enola-dev/enola/blob/main/java/dev/enola/common/exec/ProcessRequest.java
// https://github.com/enola-dev/enola/blob/main/java/dev/enola/common/linereader/ExecutionContext.java
public record ExecutableContext(Path cwd, StdIO stdIO, List<String> args, Map<String, String> env) {

    // TODO Supplier<Path> cwd() to support lazy evaluation...

    public String[] argsAsArray() {
        return args.toArray(new String[0]);
    }

    public static class Builder {
        private Path cwd = Paths.get("").toAbsolutePath();
        private StdIO stdIO = StdIO.system();
        private final List<String> args = new ArrayList<>();
        private Map<String, String> env = System.getenv();

        public Builder cwd(Path cwd) {
            this.cwd = cwd;
            return this;
        }

        public Builder stdIO(StdIO stdIO) {
            this.stdIO = stdIO;
            return this;
        }

        public Builder addArgs(List<String> args) {
            this.args.addAll(args);
            return this;
        }

        public Builder addArgs(String[] args) {
            for (String arg : args) this.args.add(arg);
            return this;
        }

        public Builder addArg(String arg) {
            this.args.add(arg);
            return this;
        }

        public Builder env(Map<String, String> env) {
            this.env = env;
            return this;
        }

        public ExecutableContext build() {
            return new ExecutableContext(cwd, stdIO, args, env);
        }
    }
}
