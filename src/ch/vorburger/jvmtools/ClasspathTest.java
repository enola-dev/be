package ch.vorburger.jvmtools;

public class ClasspathTest {

    public static void main(String[] args) {
        var classpath = Classpath.from(ClasspathTest.class.getClassLoader());
        Utils.assertTrue(classpath.get("ch/vorburger/jvmtools/ClasspathTest.class") != null);
        Utils.assertTrue(classpath.get("ch/vorburger/jvmtools/ClasspathTest.java") != null);
    }
}
