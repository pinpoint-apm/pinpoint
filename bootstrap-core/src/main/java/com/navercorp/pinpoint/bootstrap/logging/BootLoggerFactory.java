package com.navercorp.pinpoint.bootstrap.logging;

public class BootLoggerFactory {
    private static PLoggerBinder loggerBinder;

    public static void initialize(PLoggerBinder loggerBinder) {
        if (BootLoggerFactory.loggerBinder == null) {
            BootLoggerFactory.loggerBinder = loggerBinder;
        } else {
            throw new RuntimeException("loggerBinder is null");
        }
    }

    public static void unregister(PLoggerBinder loggerBinder) {
        // Limited to remove only the ones already registered
        // when writing a test case, logger register/unregister logic must be located in beforeClass and afterClass
        if (loggerBinder == BootLoggerFactory.loggerBinder) {
            BootLoggerFactory.loggerBinder = null;
        }
    }

    public static PLogger getLogger(String name) {
        if (loggerBinder == null) {
            // this prevents null exception: need to return Dummy until a Binder is assigned
            return DummyPLogger.INSTANCE;
        }
        return loggerBinder.getLogger(name);
    }

    public static PLogger getLogger(Class clazz) {
        if (clazz == null) {
            throw new NullPointerException("class must not be null");
        }
        return getLogger(clazz.getName());
    }
}
