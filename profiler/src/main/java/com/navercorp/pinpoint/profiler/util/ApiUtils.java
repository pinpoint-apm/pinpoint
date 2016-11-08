/*
 * Copyright 2014 NAVER Corp.
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
 */

package com.navercorp.pinpoint.profiler.util;

import java.util.Arrays;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public final class ApiUtils {

    private final static String EMPTY_ARRAY = "()";

    private ApiUtils() {
    }

    public static String mergeParameterVariableNameDescription(String[] parameterType, String[] variableName) {
        if (parameterType == null && variableName == null) {
            return EMPTY_ARRAY;
        }
        if (variableName != null && parameterType != null) {
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
        throw new IllegalArgumentException("invalid null pair parameterType:" + Arrays.toString(parameterType) + ", variableName:" + Arrays.toString(variableName));
    }

    public static String mergeApiDescriptor(String className, String methodName, String parameterDescriptor) {
        StringBuilder buffer = new StringBuilder(256);
        buffer.append(className);
        buffer.append('.');
        buffer.append(methodName);
        buffer.append(parameterDescriptor);
        return buffer.toString();
    }

    public static String toMethodDescriptor(String className, String methodName, String[] parameterType) {
        StringBuilder buffer = new StringBuilder(256);
        buffer.append(className);
        buffer.append('.');
        buffer.append(methodName);

        if (parameterType == null || parameterType.length == 0) {
            buffer.append(EMPTY_ARRAY);
        } else {
            buffer.append('(');
            int end = parameterType.length - 1;
            for (int i = 0; i < parameterType.length; i++) {
                buffer.append(parameterType[i]);
                if (i < end) {
                    buffer.append(", ");
                }
            }
            buffer.append(')');
        }

        return buffer.toString();
    }

    public static String toMethodDescriptor(String apiDescriptor) {
        if (apiDescriptor == null) {
            return "";
        }

        final int methodDescBegin = apiDescriptor.indexOf('(');
        if (methodDescBegin == -1) {
            throw new IllegalArgumentException("invalid api descriptor=" + apiDescriptor);
        }
        final int methodDescEnd = apiDescriptor.indexOf(')', methodDescBegin);
        if (methodDescEnd == -1) {
            throw new IllegalArgumentException("invalid api descriptor=" + apiDescriptor);
        }
        final int classNameEnd = apiDescriptor.lastIndexOf('.', methodDescBegin);
        if (classNameEnd == -1) {
            throw new IllegalArgumentException("invalid api descriptor=" + apiDescriptor);
        }

        final String className = apiDescriptor.substring(0, classNameEnd);
        final String methodName = apiDescriptor.substring(classNameEnd + 1, methodDescBegin);
        final String methodDesc = apiDescriptor.substring(methodDescBegin + 1, methodDescEnd);
        final String[] parameterTypes = methodDesc.split(",");
        for (int i = 0; i < parameterTypes.length; i++) {
            final String parameterType = parameterTypes[i];
            parameterTypes[i] = extractParameterClass(parameterType);
        }
        return toMethodDescriptor(className, methodName, parameterTypes);
    }

    private static String extractParameterClass(String parameterType) {
        parameterType = safeTrim(parameterType);
        final int classEndIndex = parameterType.indexOf(' ');
        if (classEndIndex != -1) {
            return parameterType.substring(0, classEndIndex);
        }
//        else {
////          TODO Is it error ?
//            throw new IllegalArgumentException("parameter variable name not found " + parameterType);
//        }
        return parameterType;
    }

    private static String safeTrim(String parameterType) {
        if (parameterType == null ||  parameterType.isEmpty()) {
            return parameterType;
        }
        return parameterType.trim();
    }
}