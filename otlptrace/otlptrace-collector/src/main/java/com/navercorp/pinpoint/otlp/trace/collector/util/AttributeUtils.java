package com.navercorp.pinpoint.otlp.trace.collector.util;

import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueType;

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

    public static long getAttributeIntValue(Map<String, AttributeValue> attributes, String key, long defaultValue) {
        final AttributeValue value = attributes.get(key);
        if (value != null && value.getType() == AttributeValueType.LONG) {
            return (Long) value.getValue();
        }
        return defaultValue;
    }

    public static String getAttributeStringValue(Map<String, AttributeValue> attributes, String key, String defaultValue) {
        final AttributeValue value = attributes.get(key);
        if (value != null && value.getType() == AttributeValueType.STRING) {
            return (String) value.getValue();
        }
        return defaultValue;
    }
}
