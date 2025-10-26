package ch.vorburger.main;

/**
 * Test helper to invoke {@link ch.vorburger.main.Main} implementations and capture their output.
 *
 * <p>Only intended for use in tests. Only suitable for Main with String stdout/stderr. For more
 * complex cases, consider just using {@link ch.vorburger.main.ExecutableContext} with a suitable
 * {@link ch.vorburger.main.StdIO} implementation directly.
 */
public class MainTester {

    // Originally inspired by
    //   https://github.com/enola-dev/enola/blob/main/java/dev/enola/cli/common/CLI.java

    public static record Result(int exitCode, String stdout, String stderr) {}

    public Result test(Main main, String... args) throws Exception {
        var stdIO = StdIO.inMemory();
        var ctx = new ExecutableContext.Builder().stdIO(stdIO).addArgs(args).build();
        var exitCode = main.invoke(ctx);
        return new Result(exitCode, stdIO.outString(), stdIO.errString());
    }
}
