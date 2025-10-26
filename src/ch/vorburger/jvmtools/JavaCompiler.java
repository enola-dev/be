package ch.vorburger.jvmtools;

import ch.vorburger.main.StdIO;
import ch.vorburger.stereotype.Service;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

public class JavaCompiler implements Service<JavaCompiler.Input, Boolean> {

    // TODO Do NOT extends Task<JavaCompiler.Input, Void?> here to avoid dependency on
    // dev.enola.be.task in this module, but rather have a separate JavaCompilerTask in
    // dev.enola.be.jvm module that uses this JavaCompiler class. Note that they have different
    // lifecycles - this is a Singleton service, while Task is per-invocation.

    public record Input(
            StdIO stdIO, Sourcepath sourcepath /*, Classpath classpath*/, Options options) {

        public static class Builder {
            private StdIO stdIO = StdIO.system();
            private Sourcepath sourcepath = new Sourcepath();
            private Path outputDirectory;

            public Builder stdIO(StdIO stdIO) {
                this.stdIO = stdIO;
                return this;
            }

            public Builder source(Stream<Path> paths) {
                // TODO Optimization: Keep Stream, and start compiling right away from it...
                paths.forEach(this.sourcepath::addPath);
                return this;
            }

            public Builder source(Path path) {
                this.sourcepath.addPath(path);
                return this;
            }

            public Sourcepath sourcepath() {
                return sourcepath;
            }

            public Builder sourcepath(Sourcepath sourcepath) {
                this.sourcepath = sourcepath;
                return this;
            }

            public Builder outputDirectory(Path outputDirectory) {
                this.outputDirectory = outputDirectory;
                return this;
            }

            public Builder outputDirectory(String outputDirectory) {
                return outputDirectory(Path.of(outputDirectory));
            }

            public Input build() {
                var options = new Options(outputDirectory);
                return new Input(stdIO, sourcepath, options);
            }
        }
    }

    // TODO Support in-memory output; see https://www.baeldung.com/java-string-compile-execute-code
    public static record Options(Path outputDirectory) {}

    @Override
    public Boolean invoke(Input input) throws Exception {
        var err = input.stdIO().errWriter();
        var optionsList = new ArrayList<String>();
        if (input.options != null) {
            var options = input.options;
            if (options.outputDirectory != null) {
                optionsList.add("-d");
                optionsList.add(options.outputDirectory.toString());
            }
        }
        var aptClasses = new ArrayList<String>(); // TODO
        var compilationUnits = input.sourcepath.getJavaFileObjects();

        javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        var diagnosticListener = new StdIODiagnosticListener(input.stdIO());
        var fileManager = compiler.getStandardFileManager(diagnosticListener, null, null);

        var task =
                compiler.getTask(
                        err,
                        fileManager,
                        diagnosticListener,
                        optionsList,
                        aptClasses,
                        compilationUnits);
        // TODO task.addModules(moduleNames);
        // TODO task.setProcessors(aptProcessors);
        return task.call();
    }

    private static record StdIODiagnosticListener(StdIO stdIO)
            implements DiagnosticListener<JavaFileObject> {

        @Override
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
            if (isError(diagnostic.getKind())) print(stdIO.errPrintStream(), diagnostic);
            else print(stdIO.outPrintStream(), diagnostic);
        }

        private void print(PrintStream ps, Diagnostic<? extends JavaFileObject> diagnostic) {
            ps.printf(
                    "%s: %s:%d:%d: %s%n",
                    diagnostic.getKind(),
                    diagnostic.getSource().toUri(),
                    diagnostic.getLineNumber(),
                    diagnostic.getColumnNumber(),
                    diagnostic.getCode(),
                    diagnostic.getMessage(null));
        }

        private boolean isError(Kind kind) {
            return kind == Kind.ERROR || kind == Kind.WARNING || kind == Kind.MANDATORY_WARNING;
        }
    }
}
