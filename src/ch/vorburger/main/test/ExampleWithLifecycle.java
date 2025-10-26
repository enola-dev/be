package ch.vorburger.main.test;

import ch.vorburger.main.Main;
import ch.vorburger.stereotype.Lifecycled;

public class ExampleWithLifecycle extends Example implements Lifecycled {

    public static void main(String[] args) throws Exception {
        try (var example = new ExampleWithLifecycle()) {
            example.init();
            Main.main(example, args);
        }
    }

    @Override
    public void init() throws Exception {
        System.out.println("ExampleWithLifecycle init");
    }

    @Override
    public void close() throws Exception {
        System.out.println("ExampleWithLifecycle close");
    }
}
