package ch.vorburger.jvmtools;

import static ch.vorburger.jvmtools.Utils.assertTrue;

import java.nio.file.Path;

public class JavaCompilerTest {

    public static void main(String[] args) throws Exception {
        var compiler = new JavaCompiler();

        var sourcepath = new Sourcepath();
        // sourcepath.addClasspathResource("ch/vorburger/jvmtools/Hello.java");
        sourcepath.addPath(Path.of("src/ch/vorburger/jvmtools/Hello.java"));

        var outputDirectory = Path.of(".build/JavaCompilerTest");
        var options = new JavaCompiler.Options.Builder().outputDirectory(outputDirectory).build();
        var stdIO = ch.vorburger.main.StdIO.inMemory();
        assert compiler.invoke(new JavaCompiler.Input(stdIO, sourcepath /*, classpath*/, options))
                == true;
        stdIO.assertErrorEmpty();

        var outputClassFile = outputDirectory.resolve("ch/vorburger/jvmtools/Hello.class");
        assertTrue(outputClassFile.toFile().exists());
    }
}
