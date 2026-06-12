package com.navercorp.pinpoint.otlp.trace.collector.util;

import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueLong;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValueString;

import java.util.Map;

public final class AttributeUtils {
    private AttributeUtils() {
    }

    public static long getIntValue(Map<String, Object> attributes, String key, long defaultValue) {
        final Object value = attributes.get(key);
        if (value instanceof Number) {
            return (long) value;
        }
        return defaultValue;
    }

    public static String getStringValue(Map<String, Object> attributes, String key, String defaultValue) {
        final Object value = attributes.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    public static long getAttributeIntValue(Map<String, AttributeValue> attributes, String key, long defaultValue) {
        final AttributeValue value = attributes.get(key);
        if (value instanceof AttributeValueLong longValue) {
            return longValue.getLongValue();
        }
        return defaultValue;
    }

    public static String getAttributeStringValue(Map<String, AttributeValue> attributes, String key, String defaultValue) {
        final AttributeValue value = attributes.get(key);
        if (value instanceof AttributeValueString stringValue) {
            return stringValue.getStringValue();
        }
        return defaultValue;
    }
}
