package ch.vorburger.jvmtools;

import static ch.vorburger.test.Assert.assertTrue;

import java.io.IOException;

public class SourcepathTest {

    public static void main(String[] args) throws IOException {
        var sourcepath = new Sourcepath();
        sourcepath.addClasspathResource(
                SourcepathTest.class.getClassLoader(), "ch/vorburger/jvmtools/Hello.java");
        var iterator = sourcepath.getJavaFileObjects().iterator();
        var jfo = iterator.next();
        assertTrue(jfo.getName().endsWith("/ch/vorburger/jvmtools/Hello.java"));
        assertTrue(jfo.getCharContent(false).toString().contains("public class Hello {"));
        assertTrue(iterator.hasNext() == false);
    }
}
