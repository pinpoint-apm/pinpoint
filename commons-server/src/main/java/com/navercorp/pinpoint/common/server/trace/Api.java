package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Objects;

public class Api {
    private final String method;
    private final String className;
    private final String description;
    private final MethodTypeEnum methodTypeEnum;
    private int lineNumber;
    private String location;

    private Api(String method, String className, String description, MethodTypeEnum methodTypeEnum) {
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

    public int getLineNumber() {
        return lineNumber;
    }

    public String getLocation() {
        return location;
    }

    public static class Builder {
        private final String method;
        private final String className;
        private final String description;
        private final MethodTypeEnum methodTypeEnum;
        private String location;
        private int lineNumber;

        public Builder(String method, String className, String description, MethodTypeEnum methodTypeEnum) {
            this.method = Objects.requireNonNull(method, "title");
            this.className = Objects.requireNonNull(className, "className");
            this.description = Objects.requireNonNull(description, "description");
            this.methodTypeEnum = Objects.requireNonNull(methodTypeEnum, "methodTypeEnum");
            this.lineNumber = 0;
            this.location = null;
        }

        public Builder setLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        public Api build() {
            Api result = new Api(this.method, this.className, this.description, this.methodTypeEnum);
            result.lineNumber = Math.max(this.lineNumber, 0);
            result.location = StringUtils.defaultIfEmpty(this.location, "");
            return result;
        }
    }
}
