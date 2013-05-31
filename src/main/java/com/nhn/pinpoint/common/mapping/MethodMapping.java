package com.nhn.pinpoint.common.mapping;

import java.util.Arrays;

/**
 *
 */
public class MethodMapping {
    private String methodName;
    private String[] parameterType;
    private String[] parameterName;

    private int methodId;
    private ClassMapping classMapping;

    public MethodMapping(String methodName, String[] parameterType) {
        this.methodName = methodName;
        this.parameterType = parameterType;
    }

    public MethodMapping(String methodName, String[] parameterType, String[] parameterName) {
        this.methodName = methodName;
        this.parameterType = parameterType;
        this.parameterName = parameterName;
    }

    public ClassMapping getClassMapping() {
        return classMapping;
    }

    public void setClassMapping(ClassMapping classMapping) {
        this.classMapping = classMapping;
    }

    public String[] getParameterName() {
        return parameterName;
    }

    public void setParameterName(String[] parameterName) {
        this.parameterName = parameterName;
    }

    public String[] getParameterType() {
        return parameterType;
    }

    public void setParameterType(String[] parameterType) {
        this.parameterType = parameterType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodMapping that = (MethodMapping) o;

        if (!Arrays.equals(parameterType, that.parameterType)) return false;
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = methodName != null ? methodName.hashCode() : 0;
        result = 31 * result + (parameterType != null ? Arrays.hashCode(parameterType) : 0);
        return result;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    public int getMethodId() {
        return methodId;
    }
}

