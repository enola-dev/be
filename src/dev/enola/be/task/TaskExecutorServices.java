package dev.enola.be.task;

import static java.util.concurrent.TimeUnit.SECONDS;

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

    private static final Thread.UncaughtExceptionHandler HANDLER =
            new LoggingThreadUncaughtExceptionHandler();

    static ExecutorService newVirtualThreadPerTaskExecutor() {
        ThreadFactory factory = Thread.ofVirtual().uncaughtExceptionHandler(HANDLER).factory();
        return Executors.newThreadPerTaskExecutor(factory);
    }

    private static final long CLOSE_EXECUTOR_SHUTDOWN_AWAIT_SECONDS = 7;

    static void close(ExecutorService executor) {
        // TODO Log shutdown progress & total time...
        executor.shutdown();
        try {
            // Wait for existing tasks to terminate
            if (!executor.awaitTermination(CLOSE_EXECUTOR_SHUTDOWN_AWAIT_SECONDS, SECONDS)) {

                // Cancel currently executing tasks
                executor.shutdownNow();
            }

        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private TaskExecutorServices() {}
}
