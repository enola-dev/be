package ch.vorburger.main;

import ch.vorburger.exec.ExecutableContext;
import ch.vorburger.exec.StdIO;

// https://github.com/enola-dev/enola/blob/main/java/dev/enola/cli/common/CLI.java
public class MainTester {

    public static record TestResult(int exitCode, String stdout, String stderr) {}

    public TestResult test(MainService main, String... args) throws Exception {
        var stdIO = StdIO.inMemory();
        var ctx = new ExecutableContext.Builder().stdIO(stdIO).addArgs(args).build();
        var exitCode = main.invoke(ctx);
        return new TestResult(exitCode, stdIO.outString(), stdIO.errString());
    }
}
