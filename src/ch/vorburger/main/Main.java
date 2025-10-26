package ch.vorburger.main;

/**
 * Main entry point for an application which is easily testable and embeddable.
 *
 * <p>Because Java's <code>static void main(String[] args)</code> is all wrong! ;-) USAGE:
 *
 * {@snippet lang=java :
 * public class MyApp implements Main {
 *   public static void main(String[] args) throws Exception {
 *     Main.main(new MyApp(), args);
 *   }
 *
 *   @Override
 *   public Integer invoke(ExecutableContext input) throws Exception {
 *     // ... input.args() ... input.env() ... input.cwd()  // @highlight substring="input" target="ExecutableContext"
 *     // ... input.stdIO().outPrintStream() ... etc. // @highlight substring="input" target="ExecutableContext"
 *     return 0;
 *   }
 * }
 * }
 */
public interface Main {

    Integer invoke(ExecutableContext input) throws Exception;

    public static void main(Main mainService, String[] args) throws Exception {
        var ctx = new ExecutableContext.Builder().addArgs(args).build();
        System.exit(mainService.invoke(ctx));
    }
}
