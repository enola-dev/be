package dev.enola.common.log;

import java.util.logging.Level;

public final class JulConfigurer {

    public static void configureRootLogger() {
        var logger = java.util.logging.Logger.getLogger("");
        if (logger.getHandlers().length == 0) {
            // Add a ConsoleHandler if none present yet
            // (e.g. when no logging.properties is configured)
            var handler = new java.util.logging.ConsoleHandler();
            handler.setLevel(java.util.logging.Level.ALL);
            logger.addHandler(handler);
        }
        // Remove other handlers (e.g. default ConsoleHandler
        // added by JVM when no logging.properties is configured)
        for (var handler : logger.getHandlers()) {
            if (!(handler instanceof java.util.logging.ConsoleHandler)) { // should not happen?
                logger.removeHandler(handler);
            }
        }
        logger.setLevel(Level.ALL);
    }

    private JulConfigurer() {}
}
