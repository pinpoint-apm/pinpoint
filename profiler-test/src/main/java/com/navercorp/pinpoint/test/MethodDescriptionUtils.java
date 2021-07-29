/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.common.util.ArrayUtils;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * test only
 * @author Woonduk Kang(emeroad)
 */
final class MethodDescriptionUtils {

    private final static String EMPTY_ARRAY = "()";

    private MethodDescriptionUtils() {
    }

    public static String toJavaMethodDescriptor(String className, String methodName, String[] parameterType) {
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(methodName, "methodName");

        StringBuilder buffer = new StringBuilder(256);
        buffer.append(className);
        buffer.append('.');
        buffer.append(methodName);

        appendParameter(buffer, parameterType);

        return buffer.toString();
    }

    /**
     * remove parameter variableName
     * @return
     */
    public static String toJavaMethodDescriptor(String apiMetaDataDescriptor) {
        if (apiMetaDataDescriptor == null) {
            return "";
        }

        final int methodDescBegin = apiMetaDataDescriptor.indexOf('(');
        if (methodDescBegin == -1) {
            throw new IllegalArgumentException("invalid api descriptor=" + apiMetaDataDescriptor);
        }
        final int methodDescEnd = apiMetaDataDescriptor.indexOf(')', methodDescBegin);
        if (methodDescEnd == -1) {
            throw new IllegalArgumentException("invalid api descriptor=" + apiMetaDataDescriptor);
        }

        final int classNameEnd = apiMetaDataDescriptor.lastIndexOf('.', methodDescBegin);
        if (classNameEnd == -1) {
            throw new IllegalArgumentException("invalid api descriptor=" + apiMetaDataDescriptor);
        }

        final String className = apiMetaDataDescriptor.substring(0, classNameEnd);
        final String methodName = apiMetaDataDescriptor.substring(classNameEnd + 1, methodDescBegin);
        final String methodDesc = apiMetaDataDescriptor.substring(methodDescBegin + 1, methodDescEnd);

        final String[] parameterTypes = methodDesc.split(",");
        for (int i = 0; i < parameterTypes.length; i++) {
            final String parameterType = parameterTypes[i];
            parameterTypes[i] = extractParameterClass(parameterType);
        }
        return toJavaMethodDescriptor(className, methodName, parameterTypes);
    }

    private static void appendParameter(StringBuilder buffer, String[] parameterType) {
        if (ArrayUtils.isEmpty(parameterType)) {
            buffer.append(EMPTY_ARRAY);
        } else {
            buffer.append('(');
            buffer.append(parameterType[0]);
            for (int i = 1; i < parameterType.length; i++) {
                buffer.append(", ");
                buffer.append(parameterType[i]);
            }
            buffer.append(')');
        }
    }

    private static String extractParameterClass(String parameterType) {
        parameterType = safeTrim(parameterType);
        final int classEndIndex = parameterType.indexOf(' ');
        if (classEndIndex != -1) {
            return parameterType.substring(0, classEndIndex);
        }
        return parameterType;
    }

    private static String safeTrim(String parameterType) {
        if (StringUtils.isEmpty(parameterType)) {
            return parameterType;
        }
        return parameterType.trim();
    }


    public static String getConstructorSimpleName(Constructor<?> constructor) {
        final String name = constructor.getName();
        return getConstructorSimpleName(name);
    }

    static String getConstructorSimpleName(String name) {
        final int startIndex = name.lastIndexOf('.');
        if (startIndex == -1) {
            return name;
        } else {
            return name.substring(startIndex + 1, name.length());
        }
    }
}
