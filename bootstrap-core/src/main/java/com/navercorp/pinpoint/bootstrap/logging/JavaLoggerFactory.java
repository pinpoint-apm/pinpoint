package com.navercorp.pinpoint.bootstrap.logging;

import java.util.logging.Logger;

public class JavaLoggerFactory {
    private static final JavaLoggerBinder loggerBinder = new JavaLoggerBinder();

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
