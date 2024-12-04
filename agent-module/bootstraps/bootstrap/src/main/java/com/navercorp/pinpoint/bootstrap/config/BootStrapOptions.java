package com.navercorp.pinpoint.bootstrap.config;

import java.util.Objects;
import java.util.Properties;

public class BootStrapOptions {
    private final Properties properties;

    private static final String PINPOINT_DISABLE = "pinpoint.disable";


    public BootStrapOptions(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    public boolean getPinpointDisable() {
        return Boolean.parseBoolean(properties.getProperty(PINPOINT_DISABLE, Boolean.FALSE.toString()));
    }

}
