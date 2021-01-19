package com.navercorp.pinpoint.bootstrap.config.util;

public class PlaceHolder {
    public static final String START = "${";
    public static final String END = "}";
    public static final String DELIMITER = ":";

    private final String key;
    private final String defaultValue;

    public PlaceHolder(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}