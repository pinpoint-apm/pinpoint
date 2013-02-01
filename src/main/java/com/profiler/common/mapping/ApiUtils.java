package com.profiler.common.mapping;

/**
 *
 */
public class ApiUtils {

    private final static String EMTPY_ARRAY = "()";
    private static final int METHOD_RANGE = 100;

    public static int parseClassId(int apiId) {
        if (apiId == 0) {
            throw new IllegalArgumentException();
        }
        return apiId / METHOD_RANGE;
    }

    public static int parseMethodId(int apiId) {
        int i = parseClassId(apiId) * METHOD_RANGE;
        return apiId - i;
    }

    public static int getApiId(int classId, int methodId) {
        return classId * METHOD_RANGE + methodId;
    }

    public static String mergeParameterVariableNameDescription(String[] paramterType, String[] variableName) {
        if (paramterType == null && variableName == null) {
            return EMTPY_ARRAY;
        }
        if (paramterType.length != variableName.length) {
            throw new IllegalArgumentException("args size not equal");
        }
        if (paramterType.length == 0) {
            return EMTPY_ARRAY;
        }

        StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        int end = paramterType.length - 1;
        for (int i = 0; i < paramterType.length; i++) {
            sb.append(paramterType[i]);
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
