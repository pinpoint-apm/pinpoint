package com.navercorp.pinpoint.metric.common.model;

import java.util.Objects;

public enum TelegrafHttpFieldType {
    INT(1, "int"),
    FLOAT(2, "float"),
    UNKNOWN(-1, "UNKNOWN");

    private final int code;
    private final String name;

    TelegrafHttpFieldType(int code, String type) {
        this.code = code;
        this.name = Objects.requireNonNull(type, "type");
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return name;
    }
}
