package dev.enola.be.io;

import static ch.vorburger.test.Assert.assertTrue;

import java.nio.file.Path;

public class FileSetTest {

    // https://github.com/enola-dev/enola/blob/main/java/dev/enola/common/io/resource/stream/FileGlobResolverTest.java

    // TODO @ClassRule public static final TemporaryFolder tempFolder = new TemporaryFolder();

    public static void main(String[] args) {
        var fileSet =
                new FileSet.Builder()
                        .addRoot(Path.of("src/dev/enola/be"))
                        .includeGlob("io/File*.java")
                        .excludeGlob("**/File*Test.java")
                        .build();

        var paths = fileSet.stream().toList();
        assertTrue(paths.size() == 1);
        assertTrue(paths.get(0).equals(Path.of("src/dev/enola/be/io/FileSet.java")));
    }
}
