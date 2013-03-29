package com.profiler.logging;

/**
 *
 */
public final class LoggerFactory {

    private static LoggerBinder loggerBinder;

    public static void initialize(LoggerBinder loggerBinder) {
        if (LoggerFactory.loggerBinder == null) {
            System.out.println("set logger binder-------------");
            LoggerFactory.loggerBinder = loggerBinder;
        }
    }

    public static Logger getLogger(String name) {
        return loggerBinder.getLogger(name);
    }

    public static Logger getLogger(Class name) {
        return getLogger(name.getName());
    }
}
