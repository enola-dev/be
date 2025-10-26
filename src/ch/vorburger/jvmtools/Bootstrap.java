package ch.vorburger.jvmtools;

import static ch.vorburger.test.Assert.assertTrue;

import dev.enola.be.io.FileSet;

public class Bootstrap {

    public static void main(String[] args) throws Exception {

        // TODO Use a (TBD) JavaCompilerTask instead of JavaCompiler directly here...

        var input =
                new JavaCompiler.Input.Builder()
                        .sources(new FileSet.Builder().addRoot("src").includeGlob("**/*.java"))
                        .outputDirectory(".build/bootstrap-classes")
                        .build();
        assertTrue(new JavaCompiler().invoke(input));

        // TODO Test loading and running compiled classes, in parallel!

        // TODO JAR
    }
}
