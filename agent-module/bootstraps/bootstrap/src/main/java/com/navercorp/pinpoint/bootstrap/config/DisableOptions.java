package com.navercorp.pinpoint.bootstrap.config;

import java.util.function.Function;

public class DisableOptions {
    public static final String SYSTEM = "pinpoint.disable";
    public static final String ENV = "PINPOINT_DISABLE";
    public static final String CONFIG = SYSTEM;

    private DisableOptions() {
    }

    public static boolean isBootDisabled() {
        if (isDisabled(System::getProperty, SYSTEM)) {
            return true;
        }
        if (isDisabled(System::getenv, ENV)) {
            return true;
        }
        return false;
    }

    public static boolean isDisabled(Function<String, String> properties, String key) {
        String value = properties.apply(key);
        return Boolean.parseBoolean(value);
    }
}
