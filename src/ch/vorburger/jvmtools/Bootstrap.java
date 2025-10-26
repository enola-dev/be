package ch.vorburger.jvmtools;

import ch.vorburger.main.StdIO;

public class Bootstrap {

    public static void main(String[] args) throws Exception {
        var compiler = new JavaCompiler();

        // TODO Glob
        var sourcepath = new Sourcepath();
        sourcepath.addClasspathResource("ch/vorburger/jvmtools/JavaCompiler.java");
        sourcepath.addClasspathResource("ch/vorburger/jvmtools/JavaCompilerTest.java");

        var options =
                new JavaCompiler.Options.Builder()
                        .outputDirectory(".build/bootstrap-classes")
                        .build();

        compiler.invoke(
                new JavaCompiler.Input(StdIO.system(), sourcepath /* TODO, classpath */, options));

        // TODO JAR
    }
}
