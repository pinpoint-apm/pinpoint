package com.navercorp.pinpoint.otlp.trace.collector.util;

import java.util.Map;

public final class AttributeUtils {
    private AttributeUtils() {
    }

    public static long getIntValue(Map<String, Object> attributes, String key, long defaultValue) {
        final Object value = attributes.get(key);
        if (value != null && value instanceof Number) {
            return (long) value;
        }
        return defaultValue;
    }

    public static String getStringValue(Map<String, Object> attributes, String key, String defaultValue) {
        final Object value = attributes.get(key);
        if (value != null && value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }
}
