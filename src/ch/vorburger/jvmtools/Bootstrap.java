package ch.vorburger.jvmtools;

import ch.vorburger.main.StdIO;

public class Bootstrap {

    public static void main(String[] args) throws Exception {
        var compiler = new JavaCompiler();

        // TODO Glob
        var sourcepath = new Sourcepath();
        sourcepath.addClasspathResource("ch/vorburger/jvmtools/JavaCompiler.java");
        sourcepath.addClasspathResource("ch/vorburger/jvmtools/JavaCompilerTest.java");

        // TODO var classpath = new Classpath();
        // classpath.setOutputDirectory(".build/bootstrap-classes");

        compiler.invoke(new JavaCompiler.Input(StdIO.system(), sourcepath /* TODO, classpath */));

        // TODO JAR
    }
}
