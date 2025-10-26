package ch.vorburger.jvmtools;

import static ch.vorburger.test.Assert.assertExists;
import static ch.vorburger.test.Assert.assertTrue;

import java.nio.file.Path;

public class JavaCompilerTest {

    public static void main(String[] args) throws Exception {

        var sourcepath = new Sourcepath();
        // sourcepath.addClasspathResource("ch/vorburger/jvmtools/Hello.java");
        sourcepath.addPath(Path.of("src/ch/vorburger/jvmtools/Hello.java"));

        // TODO Use JUnit utility for auto-cleaned test specific output directory
        //   @ClassRule public static final TemporaryFolder tempFolder = new TemporaryFolder();
        var outputDirectory = Path.of(".build/JavaCompilerTest");
        var outputClassFile = outputDirectory.resolve("ch/vorburger/jvmtools/Hello.class").toFile();
        outputClassFile.delete();
        assertTrue(!outputClassFile.exists());

        var stdIO = ch.vorburger.main.StdIO.inMemory();
        var input =
                new JavaCompiler.Input.Builder()
                        .stdIO(stdIO)
                        .sourcepath(sourcepath)
                        .outputDirectory(outputDirectory)
                        .build();
        assertTrue(new JavaCompiler().invoke(input) == true);
        stdIO.assertErrorEmpty();

        assertExists(outputClassFile);
    }
}
