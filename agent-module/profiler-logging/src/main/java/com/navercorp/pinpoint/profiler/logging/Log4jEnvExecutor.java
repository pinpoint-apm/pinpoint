package com.navercorp.pinpoint.profiler.logging;

import java.util.function.Supplier;

public class Log4jEnvExecutor {

    /**
     * log4j 2.24
     * ref : {@value org.apache.logging.log4j.spi.Provider#PROVIDER_PROPERTY_NAME}
     */
    public static final String PROVIDER_PROPERTY_NAME = "log4j.provider";
    public static final String FACTORY_PROPERTY_NAME = "log4j2.loggerContextFactory";

    public <V> V call(Supplier<V> supplier) {
        final String provider = prepare(PROVIDER_PROPERTY_NAME, Log4j2Provider.class.getName());
        final String factory = prepare(FACTORY_PROPERTY_NAME, null);
        try {
            return supplier.get();
        } finally {
            rollback(FACTORY_PROPERTY_NAME, factory);
            rollback(PROVIDER_PROPERTY_NAME, provider);
        }
    }

    private String prepare(String key, String value) {
        final String backup = System.getProperty(key);
        if (value != null) {
            System.setProperty(key, value);
        }
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
