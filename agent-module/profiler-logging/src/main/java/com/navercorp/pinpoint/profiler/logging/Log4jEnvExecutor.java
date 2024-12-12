package com.navercorp.pinpoint.profiler.logging;

import java.util.function.Supplier;

public class Log4jEnvExecutor {

    public static final String FACTORY_PROPERTY_NAME = "log4j2.loggerContextFactory";


    public <V> V call(Supplier<V> supplier) {
        final String factory = prepare(FACTORY_PROPERTY_NAME, Log4j2ContextFactory.class.getName());
        try {
            return supplier.get();
        } finally {
            rollback(FACTORY_PROPERTY_NAME, factory);
        }
    }

    private String prepare(String key, String value) {
        final String backup = System.getProperty(key);
        System.setProperty(key, value);
        return backup;
    }

    private void rollback(String key, String backup) {
        if (backup != null) {
            System.setProperty(key, backup);
        } else {
            System.clearProperty(key);
        }
    }

}
