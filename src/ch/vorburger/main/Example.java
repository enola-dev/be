package ch.vorburger.main;

import ch.vorburger.exec.ExecutableContext;

public class Example implements MainService {

    public static void main(String[] args) throws Exception {
        MainService.main(new Example(), args);
    }

    @Override
    public Integer invoke(ExecutableContext input) throws Exception {
        var args = input.args();
        String msg = args.isEmpty() ? "world" : args.get(0);
        input.stdIO().outPrintStream().println("hello, " + msg);
        return 123;
    }
}
