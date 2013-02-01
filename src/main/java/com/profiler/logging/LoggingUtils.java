package com.profiler.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class LoggingUtils {

    public static boolean isDebug(Logger logger) {
        return logger.isLoggable(Level.FINE);
    }

    public static void before(Logger logger, String method) {

    }

}
