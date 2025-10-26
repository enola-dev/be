package ch.vorburger.jvmtools;

import static ch.vorburger.jvmtools.Assert.assertExists;
import static ch.vorburger.jvmtools.Assert.assertTrue;

import java.nio.file.Path;

public class JavaCompilerTest {

    public static void main(String[] args) throws Exception {
        var compiler = new JavaCompiler();

        var sourcepath = new Sourcepath();
        // sourcepath.addClasspathResource("ch/vorburger/jvmtools/Hello.java");
        sourcepath.addPath(Path.of("src/ch/vorburger/jvmtools/Hello.java"));

        // TODO Use JUnit utility for auto-cleaned test specific output directory
        var outputDirectory = Path.of(".build/JavaCompilerTest");
        var outputClassFile = outputDirectory.resolve("ch/vorburger/jvmtools/Hello.class").toFile();
        outputClassFile.delete();
        assertTrue(!outputClassFile.exists());

        var options = new JavaCompiler.Options.Builder().outputDirectory(outputDirectory).build();
        var stdIO = ch.vorburger.main.StdIO.inMemory();
        assertTrue(compiler.invoke(new JavaCompiler.Input(stdIO, sourcepath, options)) == true);
        stdIO.assertErrorEmpty();

        assertExists(outputClassFile);
    }
}
