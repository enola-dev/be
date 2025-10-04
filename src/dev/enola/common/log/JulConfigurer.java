package dev.enola.common.log;

import java.util.logging.Level;

public final class JulConfigurer {

    public static void configureRootLogger() {
        String singleLineFormat = "[%1$tF %1$tT] [%4$-7s] %3$s - %5$s%6$s%n";
        System.setProperty("java.util.logging.SimpleFormatter.format", singleLineFormat);
        var rootLogger = java.util.logging.Logger.getLogger("");

        // Remove all existing handlers (e.g. default ConsoleHandler)
        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Add a ConsoleHandler if none present yet
        // (e.g. when no logging.properties is configured)
        var handler = new java.util.logging.ConsoleHandler();
        handler.setFormatter(new java.util.logging.SimpleFormatter());
        handler.setLevel(java.util.logging.Level.ALL);
        rootLogger.addHandler(handler);

        rootLogger.setLevel(Level.ALL);
    }

    private JulConfigurer() {}
}
