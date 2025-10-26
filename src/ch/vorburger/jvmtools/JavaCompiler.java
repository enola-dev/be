package ch.vorburger.jvmtools;

import ch.vorburger.main.StdIO;
import ch.vorburger.stereotype.Service;

import java.io.PrintStream;
import java.util.ArrayList;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

public class JavaCompiler implements Service<JavaCompiler.Input, Boolean> {

    // TODO Do NOT extends Task<JavaCompiler.Input, Void?> here to avoid dependency on
    // dev.enola.be.task in this module, but rather have a separate JavaCompilerTask in
    // dev.enola.be.jvm module that uses this JavaCompiler class. Note that they have different
    // lifecycles - this is a Singleton service, while Task is per-invocation.

    public record Input(StdIO stdIO, Sourcepath sourcepath, Classpath classpath) {}

    @Override
    public Boolean invoke(Input input) throws Exception {
        var err = input.stdIO().errWriter();
        var options = new ArrayList<String>(); // TODO
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
                        options,
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
