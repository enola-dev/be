package ch.vorburger.jvmtools;

public class JavaCompilerTest {

    public static void main(String[] args) throws Exception {
        var compiler = new JavaCompiler();

        // TODO Glob
        var sourcepath = new Sourcepath();
        sourcepath.addClasspathResource("ch/vorburger/jvmtools/Hello.java");

        // TODO var classpath = new Classpath();
        // classpath.setOutputDirectory(".build/jvmtools-test-classes");

        var stdIO = ch.vorburger.main.StdIO.inMemory();
        assert compiler.invoke(new JavaCompiler.Input(stdIO, sourcepath /*, classpath*/)) == true;
        stdIO.assertErrorEmpty();

        // TODO Test presence of Hello.class file (delete it at start of test!)
    }
}
