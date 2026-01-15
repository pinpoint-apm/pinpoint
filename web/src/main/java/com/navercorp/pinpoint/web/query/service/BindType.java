package com.navercorp.pinpoint.web.query.service;

import java.util.Objects;

public enum BindType {
    SQL("sql"),
    MONGO_JSON("mongoJson");

    private static final BindType[] BIND_TYPE = BindType.values();

    private final String typeName;

    BindType(String typeName) {
        this.typeName = Objects.requireNonNull(typeName, "typeName");
    }

    public String getTypeName() {
        return typeName;
    }

    public static BindType of(String typeName) {
        for (BindType bindType : BIND_TYPE) {
            if (bindType.getTypeName().equalsIgnoreCase(typeName)) {
                return bindType;
            }
        }
        return null;
    }
}
