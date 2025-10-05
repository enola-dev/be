package dev.enola.common.concurrent;

import static java.util.Objects.requireNonNull;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

class LoggingThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    // Like dev.enola.common.concurrent.LoggingThreadUncaughtExceptionHandler in
    // https://github.com/enola-dev/enola, but without the dependency on dev.enola.common and using
    // JUL instead of SLF4J. TODO Maybe move this to dev.enola.common, later?

    private final Logger logger;

    private LoggingThreadUncaughtExceptionHandler(Logger logger) {
        this.logger = requireNonNull(logger, "logger");
    }

    /** Factory method to obtain an instance of this bound to the passed JUL Logger. */
    public static UncaughtExceptionHandler toLogger(Logger logger) {
        return new LoggingThreadUncaughtExceptionHandler(logger);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        logger.log(
                Level.SEVERE, "Uncaught exception in thread '" + thread.getName() + "'", throwable);
    }
}
