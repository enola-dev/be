package ch.vorburger.jvmtools;

public class JavaCompilerTest {

    public static void main(String[] args) {
        var compiler = new JavaCompiler();

        // TODO Glob
        var sourcepath = new Sourcepath();
        sourcepath.addResource("ch/vorburger/jvmtools/Hello.java");

        var classpath = new Classpath();
        classpath.setOutputDirectory(".build/jvmtools-test-classes");

        compiler.compile(new JavaCompiler.Input(sourcepath, classpath));

        // TODO Test presence of Hello.class file (delete it at start of test!)
    }
}
