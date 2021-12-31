package com.navercorp.pinpoint.web.query;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public enum BindType {
    SQL("sql"),
    MONGO_JSON("mongoJson");

    private static final Set<BindType> BIND_TYPE = EnumSet.allOf(BindType.class);

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
