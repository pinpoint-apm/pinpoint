package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;

final class ReflectionUtils {
    public static final String ARRAY_POSTFIX = "[]";

    private ReflectionUtils() {
    }

    @VisibleForTesting
    static String getParameterTypeName(Class<?> parameterType) {
        if (parameterType == null) {
            throw new NullPointerException("parameterType");
        }

        if (!parameterType.isArray()) {
            return parameterType.getName();
        }

        // get arrayDepth & arrayType
        int arrayDepth = 0;
        while (parameterType.isArray()) {
            parameterType = parameterType.getComponentType();
            arrayDepth++;
        }

        final int bufferSize = getBufferSize(parameterType.getName(), arrayDepth);
        final StringBuilder buffer = new StringBuilder(bufferSize);

        buffer.append(parameterType.getName());
        for (int i = 0; i < arrayDepth; i++) {
            buffer.append(ARRAY_POSTFIX);
        }
        return buffer.toString();
    }

    private static int getBufferSize(String paramTypeName, int arrayDepth) {
        return paramTypeName.length() + (ARRAY_POSTFIX.length() * arrayDepth);
    }

}
