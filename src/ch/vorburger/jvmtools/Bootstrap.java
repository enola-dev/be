package ch.vorburger.jvmtools;

import static ch.vorburger.test.Assert.assertTrue;

import java.nio.file.Path;

public class Bootstrap {

    public static void main(String[] args) throws Exception {

        // TODO Use a (TBD) JavaCompilerTask instead of JavaCompiler directly here...

        var input =
                new JavaCompiler.Input.Builder()
                        // TODO FileSet Glob
                        .source(Path.of("src/ch/vorburger/jvmtools/JavaCompiler.java"))
                        .source(Path.of("src/ch/vorburger/jvmtools/JavaCompilerTest.java"))
                        .outputDirectory(".build/bootstrap-classes")
                        .build();
        assertTrue(new JavaCompiler().invoke(input));

        // TODO Test loading and running compiled classes, in parallel!

        // TODO JAR
    }
}
