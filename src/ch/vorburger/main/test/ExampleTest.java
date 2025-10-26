package ch.vorburger.main.test;

import ch.vorburger.main.MainTester;

public class ExampleTest {

    static MainTester t = new MainTester();

    public static void main(String[] args) throws Exception {
        var result = t.test(new Example(), "world");
        assert result.exitCode() == 123;
        assert "hello, world\n".equals(result.stdout());
    }
}
