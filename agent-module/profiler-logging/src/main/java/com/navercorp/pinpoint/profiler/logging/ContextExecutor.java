package com.navercorp.pinpoint.profiler.logging;

import java.util.function.Supplier;

public class ContextExecutor {

    public static final String FACTORY_PROPERTY_NAME = "log4j2.loggerContextFactory";
    public static final String NOLOOKUPS = "log4j2.formatMsgNoLookups";


    public <V> V call(Supplier<V> supplier) {
        final String factory = prepare(FACTORY_PROPERTY_NAME, Log4j2ContextFactory.class.getName());
        // Log4j2 RCE CVE-2021-44228
        // https://github.com/pinpoint-apm/pinpoint/issues/8489
        final String nolookup = prepare(NOLOOKUPS, Boolean.TRUE.toString());
        try {
            return supplier.get();
        } finally {
            rollback(NOLOOKUPS, nolookup);
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
