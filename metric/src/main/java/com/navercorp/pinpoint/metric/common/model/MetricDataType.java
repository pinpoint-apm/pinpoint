package com.navercorp.pinpoint.metric.common.model;

import java.util.Objects;

public enum MetricDataType {
    LONG(1, "long"),
    DOUBLE(2, "dobule"),
    UNKNOWN(100, "unknown");

    private final int code;
    private final String value;

    MetricDataType(int code, String value) {
        this.code = code;
        this.value = Objects.requireNonNull(value, "value");
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static MetricDataType getByCode(int code) {
        for (MetricDataType metricDataType : MetricDataType.values()) {
            if (metricDataType.code == code) {
                return metricDataType;
            }
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

    public static MetricDataType getByValue(String value) {
        for (MetricDataType metricDataType : MetricDataType.values()) {
            if (metricDataType.value.equalsIgnoreCase(value)) {
                return metricDataType;
            }
        }
        throw new IllegalArgumentException("Unknown value : " + value);
    }
}
