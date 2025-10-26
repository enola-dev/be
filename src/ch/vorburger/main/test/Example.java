package ch.vorburger.main.test;

import ch.vorburger.main.ExecutableContext;
import ch.vorburger.main.Main;

public class Example implements Main {

    public static void main(String[] args) throws Exception {
        Main.main(new Example(), args);
    }

    @Override
    public Integer invoke(ExecutableContext input) throws Exception {
        var args = input.args();
        String msg = args.isEmpty() ? "world" : args.get(0);
        input.stdIO().outPrintStream().println("hello, " + msg);
        return 123;
    }
}
