package ch.vorburger.jvmtools;

public class Bootstrap {

    public static void main(String[] args) throws Exception {
        var compiler = new JavaCompiler();

        // TODO Glob
        var sourcepath = new Sourcepath();
        sourcepath.addResource("ch/vorburger/jvmtools/JavaCompiler.java");
        sourcepath.addResource("ch/vorburger/jvmtools/JavaCompilerTest.java");

        var classpath = new Classpath();
        classpath.setOutputDirectory(".build/bootstrap-classes");

        // TODO compiler.invoke(new JavaCompiler.Input(StdIO.system(), sourcepath, classpath));

        // TODO JAR
    }
}
