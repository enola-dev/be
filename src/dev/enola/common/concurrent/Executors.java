package dev.enola.common.concurrent;

import static dev.enola.common.concurrent.LoggingThreadUncaughtExceptionHandler.toLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public final class Executors {

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(
            String namePrefix, Logger logger) {
        var tf = createThreadFactory(namePrefix, logger);
        return java.util.concurrent.Executors.newSingleThreadScheduledExecutor(tf);
    }

    public static ExecutorService newVirtualThreadPerTaskExecutor(
            String namePrefix, Logger logger) {
        var tf = createVirtualThreadFactory(namePrefix, logger);
        return java.util.concurrent.Executors.newThreadPerTaskExecutor(tf);
    }

    public static ExecutorService newVirtualThreadPerTaskExecutor(Logger logger) {
        var tf = createVirtualThreadFactory(logger);
        return java.util.concurrent.Executors.newThreadPerTaskExecutor(tf);
    }

    private static ThreadFactory createThreadFactory(String namePrefix, Logger logger) {
        return Thread.ofPlatform()
                .name(namePrefix, 1)
                .uncaughtExceptionHandler(toLogger(logger))
                .daemon(true)
                // TODO new ContextAwareThreadFactory() how-to?
                // TODO .inheritInheritableThreadLocals(true) ?
                .factory();
    }

    private static ThreadFactory createVirtualThreadFactory(String namePrefix, Logger logger) {
        var builder = Thread.ofVirtual();
        if (namePrefix != null) {
            builder.name(namePrefix, 1);
        }
        return builder.uncaughtExceptionHandler(toLogger(logger))
                // NB: Virtual threads are always daemon threads, so no: .setDaemon(true)
                // TODO new ContextAwareThreadFactory() how-to?
                // TODO .inheritInheritableThreadLocals(true) ?
                .factory();
    }

    private static ThreadFactory createVirtualThreadFactory(Logger logger) {
        return createVirtualThreadFactory(null, logger);
    }

    private Executors() {}
}
