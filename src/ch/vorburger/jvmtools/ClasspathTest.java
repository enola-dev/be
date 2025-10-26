package ch.vorburger.jvmtools;

import static ch.vorburger.test.Assert.assertTrue;

public class ClasspathTest {

    public static void main(String[] args) {
        var classpath = Classpath.from(ClasspathTest.class.getClassLoader());
        assertTrue(classpath.get("ch/vorburger/jvmtools/ClasspathTest.class") != null);
        assertTrue(classpath.get("ch/vorburger/jvmtools/ClasspathTest.java") != null);
    }
}
