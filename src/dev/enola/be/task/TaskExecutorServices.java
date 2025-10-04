package dev.enola.be.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

final class TaskExecutorServices {

    // Nota bene: In *THEORY* we should *NEVER* have *ANY* uncaught exceptions from Task,
    // because any exception thrown by the task's `execute()` method would be caught and
    // wrapped in an ExecutionException by the Future returned by ExecutorService.submit().
    //
    // But in practice, who knows what the future holds, so we better log them just in case;
    // just because "swallowed" lost exceptions are seriously the worst kind of bugs to diagnose!

    private static final Logger logger = Logger.getLogger(TaskExecutorServices.class.getName());

    // Like dev.enola.common.concurrent.LoggingThreadUncaughtExceptionHandler in
    // https://github.com/enola-dev/enola, but without the dependency on dev.enola.common and using
    // JUL instead of SLF4J. TODO Maybe move this to dev.enola.common, later?
    private static class LoggingThreadUncaughtExceptionHandler
            implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            logger.log(
                    Level.SEVERE,
                    "Uncaught exception in thread '" + thread.getName() + "'",
                    throwable);
        }
    }

    static ExecutorService newVirtualThreadPerTaskExecutor() {
        Thread.UncaughtExceptionHandler handler = new LoggingThreadUncaughtExceptionHandler();
        ThreadFactory factory = Thread.ofVirtual().uncaughtExceptionHandler(handler).factory();
        return Executors.newThreadPerTaskExecutor(factory);
    }

    private TaskExecutorServices() {}
}
