package com.navercorp.pinpoint.common.server.config;

import com.navercorp.pinpoint.common.util.ByteSizeUnit;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.core.env.Environment;

import java.util.Objects;

public class EnvironmentHelper {
    private final Environment environment;
    private final String prefix;

    public EnvironmentHelper(Environment environment) {
        this.environment = Objects.requireNonNull(environment, "environment");
        this.prefix = null;
    }

    public EnvironmentHelper(Environment environment, String prefix) {
        this.environment = Objects.requireNonNull(environment, "environment");
        this.prefix = Objects.requireNonNull(prefix, "prefix");
    }

    public String getString(String propertyName, String defaultValue) {
        propertyName = resolveName(propertyName);
        return environment.getProperty(propertyName, defaultValue);
    }

    private String resolveName(String propertyName) {
        if (prefix == null) {
            return propertyName;
        }
        return prefix + propertyName;
    }

    public int getInt(String propertyName, int defaultValue) {
        propertyName = resolveName(propertyName);
        final String value = environment.getProperty(propertyName);
        return NumberUtils.toInt(value, defaultValue);
    }

    public long getLong(String propertyName, long defaultValue) {
        propertyName = resolveName(propertyName);
        final String value = environment.getProperty(propertyName);
        return NumberUtils.toLong(value, defaultValue);
    }

    public boolean getBoolean(String propertyName) {
        propertyName = resolveName(propertyName);
        final String value = environment.getProperty(propertyName);

        // if a default value will be needed afterwards, may match string value instead of Utils.
        // for now stay unmodified because of no need.

        return Boolean.parseBoolean(value);
    }

    public int getByteSize(String propertyName, final int defaultValue) {
        String value = getString(propertyName, "");
        return (int) ByteSizeUnit.getByteSize(value, defaultValue);
    }
}
