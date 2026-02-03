package com.navercorp.pinpoint.otlp.trace.collector.util;

import com.google.protobuf.ByteString;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

public final class AttributeUtils {
    private AttributeUtils() {
    }

    public static ByteString getByteString(List<KeyValue> attributes, String key, ByteString defaultValue) {
        final AnyValue value = getAnyValue(attributes, key);
        if (value != null && value.hasBytesValue()) {
            return value.getBytesValue();
        }
        return defaultValue;
    }

    public static ArrayValue getArrayValue(List<KeyValue> attributes, String key, ArrayValue defaultValue) {
        final AnyValue value = getAnyValue(attributes, key);
        if (value != null && value.hasArrayValue()) {
            return value.getArrayValue();
        }
        return defaultValue;
    }

    public static boolean getBoolValue(List<KeyValue> attributes, String key, boolean defaultValue) {
        final AnyValue value = getAnyValue(attributes, key);
        if (value != null && value.hasBoolValue()) {
            return value.getBoolValue();
        }
        return defaultValue;
    }

    public static long getIntValue(List<KeyValue> attributes, String key, long defaultValue) {
        final AnyValue value = getAnyValue(attributes, key);
        if (value != null && value.hasIntValue()) {
            return value.getIntValue();
        }
        return defaultValue;
    }

    public static String getStringValue(List<KeyValue> attributes, String key, String defaultValue) {
        final AnyValue value = getAnyValue(attributes, key);
        if (value != null && value.hasStringValue()) {
            return value.getStringValue();
        }
        return defaultValue;
    }

    public static boolean isExist(List<KeyValue> attributes, String key) {
        final AnyValue value = getAnyValue(attributes, key);
        return value != null;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static AnyValue getAnyValue(List<KeyValue> attributes, String key) {
        Objects.requireNonNull(attributes, "attributes");
        Objects.requireNonNull(key, "key");

        if (attributes instanceof RandomAccess) {
            for (int i = 0; i < attributes.size(); i++) {
                final KeyValue keyValue = attributes.get(i);
                if (keyValue.getKey().equals(key)) {
                    return keyValue.getValue();
                }
            }
            return null;
        } else {
            for (KeyValue keyValue : attributes) {
                if (keyValue.getKey().equals(key)) {
                    return keyValue.getValue();
                }
            }
            return null;
        }
    }

}
