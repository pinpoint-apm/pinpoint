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
}
