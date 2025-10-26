package ch.vorburger.main;

import ch.vorburger.exec.ExecutableContext;

public interface MainService extends Service<ExecutableContext, Integer> {

    public static void main(MainService mainService, String[] args) throws Exception {
        mainService.start();
        try {
            var ctx = new ExecutableContext.Builder().addArgs(args).build();
            System.exit(mainService.invoke(ctx));
        } finally {
            mainService.close();
        }
    }
}
