package com.navercorp.pinpoint.tools.utils;

import java.util.Properties;

public class PropertyResolver {


    private static final String startToken = "${";
    private static final String endToken = "}";

    private final Properties properties;

    public PropertyResolver(Properties properties) {
        this.properties = properties;
    }

    public String resolve(String key) {
        return resolve(key, null);
    }

    public String resolve(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        if (value.startsWith(startToken) && value.endsWith(endToken)) {
            key = value.substring(startToken.length(), value.length() - endToken.length());
            value = resolve(key, defaultValue);
        }
        return value;
    }
}
