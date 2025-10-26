package ch.vorburger.main;

public class ExampleTest {

    static MainTester t = new MainTester();

    public static void main(String[] args) throws Exception {
        try (var example = new Example()) {
            example.start();

            var result = t.test(example, "world");
            assert result.exitCode() == 123;
            assert "hello, world\n".equals(result.stdout());
        }
    }
}
