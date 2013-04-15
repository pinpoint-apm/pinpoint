package com.profiler.logging;

/**
 *
 */
public final class LoggerFactory {

    private static LoggerBinder loggerBinder;

    public static void initialize(LoggerBinder loggerBinder) {
        if (LoggerFactory.loggerBinder == null) {
            LoggerFactory.loggerBinder = loggerBinder;
        }
    }

    public static void unregister(LoggerBinder loggerBinder) {
        // 등록한 놈만  제거 가능하도록 제한
        if (loggerBinder == LoggerFactory.loggerBinder) {
            LoggerFactory.loggerBinder = null;
        }

    }

    public static Logger getLogger(String name) {
        return loggerBinder.getLogger(name);
    }

    public static Logger getLogger(Class clazz) {
        if (clazz == null) {
            throw new NullPointerException("class must not be null");
        }
        return getLogger(clazz.getName());
    }
}
