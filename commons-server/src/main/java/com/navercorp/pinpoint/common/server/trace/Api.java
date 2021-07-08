package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;

import java.util.Objects;

public class Api {
    private final String method;
    private final String className;
    private final String description;
    private final MethodTypeEnum methodTypeEnum;

    public Api(String method, String className, String description, MethodTypeEnum methodTypeEnum) {
        this.method = Objects.requireNonNull(method, "title");
        this.className = Objects.requireNonNull(className, "className");
        this.description = Objects.requireNonNull(description, "description");
        this.methodTypeEnum = Objects.requireNonNull(methodTypeEnum, "methodTypeEnum");
    }

    public String getMethod() {
        return method;
    }

    public String getClassName() {
        return className;
    }

    public String getDescription() {
        return description;
    }

    public MethodTypeEnum getMethodTypeEnum() {
        return methodTypeEnum;
    }
}
