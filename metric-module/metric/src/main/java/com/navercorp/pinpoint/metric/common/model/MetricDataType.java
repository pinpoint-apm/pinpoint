package com.navercorp.pinpoint.metric.common.model;

import com.navercorp.pinpoint.common.util.apache.IntHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum MetricDataType {
    LONG(1, "long"),
    DOUBLE(2, "double"),
    UNKNOWN(-1, "unknown");

    private final int code;
    private final String type;

    private static final IntHashMap<MetricDataType> CODE_MAP = newCodeMap();
    private static final Map<String, MetricDataType> TYPE_MAP = newTypeMap();

    private static IntHashMap<MetricDataType> newCodeMap() {
        IntHashMap<MetricDataType> map = new IntHashMap<>();
        for (MetricDataType metricDataType : MetricDataType.values()) {
            map.put(metricDataType.getCode(), metricDataType);
        }
        return map;
    }

    private static Map<String, MetricDataType> newTypeMap() {
        Map<String, MetricDataType> map = new HashMap<>();
        for (MetricDataType metricDataType : MetricDataType.values()) {
            map.put(metricDataType.getType(), metricDataType);
        }
        return map;
    }

    MetricDataType(int code, String type) {
        this.code = code;
        this.type = Objects.requireNonNull(type, "type");
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public static MetricDataType getByCode(int code) {
        final MetricDataType metricDataType = CODE_MAP.get(code);
        if (metricDataType == null) {
            throw new IllegalArgumentException("Unknown code : " + code);
        }
        return metricDataType;
    }

    public static MetricDataType getByType(String name) {
        MetricDataType metricDataType = TYPE_MAP.get(name);
        if (metricDataType == null) {
            throw new IllegalArgumentException("Unknown name : " + name);
        }
        return metricDataType;
    }
}
