package ch.vorburger.main.test;

import static ch.vorburger.test.Assert.assertTrue;

import ch.vorburger.main.MainTester;

public class ExampleTest {

    static MainTester t = new MainTester();

    public static void main(String[] args) throws Exception {
        var result = t.test(new Example(), "world");
        assertTrue(result.exitCode() == 123);
        assertTrue("hello, world\n".equals(result.stdout()));
    }
}
