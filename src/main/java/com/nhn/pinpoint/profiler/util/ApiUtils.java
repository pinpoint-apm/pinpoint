package com.nhn.pinpoint.profiler.util;

/**
 * @author emeroad
 */
public final class ApiUtils {

    private final static String EMPTY_ARRAY = "()";

    private ApiUtils() {
    }

    public static String mergeParameterVariableNameDescription(String[] parameterType, String[] variableName) {
        if (parameterType == null && variableName == null) {
            return EMPTY_ARRAY;
        }
        if (parameterType.length != variableName.length) {
            throw new IllegalArgumentException("args size not equal");
        }
        if (parameterType.length == 0) {
            return EMPTY_ARRAY;
        }

        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        int end = parameterType.length - 1;
        for (int i = 0; i < parameterType.length; i++) {
            sb.append(parameterType[i]);
            sb.append(' ');
            sb.append(variableName[i]);
            if (i < end) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }

    public static String mergeApiDescriptor(String className, String methodName, String parameterDescriptor) {
        StringBuilder buffer = new StringBuilder(256);
        buffer.append(className);
        buffer.append(".");
        buffer.append(methodName);
        buffer.append(parameterDescriptor);
        return buffer.toString();
    }
}
