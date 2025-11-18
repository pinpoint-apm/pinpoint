/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.plugin.util;

import java.util.Objects;

public class MethodSignature {
    private final String name;
    private final String[] parameterTypeNames;

    public MethodSignature(String name, String[] parameterTypeNames) {
        this.name = Objects.requireNonNull(name, "methodName");
        this.parameterTypeNames = Objects.requireNonNull(parameterTypeNames, "parameterTypeNames");
    }

    public String getName() {
        return name;
    }

    public String[] getParameterTypeNames() {
        return parameterTypeNames;
    }

    @Override
    public String toString() {
        return join(this.name, this.parameterTypeNames);
    }

    public static String join(String prefix, String[] parameters) {
        if (parameters == null) {
            return prefix + "()";
        }

        final int length = parameters.length;
        if (length == 0) {
            return prefix + "()";
        } else if (length == 1) {
            return prefix + "(" + parameters[0] + ")";
        } else if (length == 2) {
            return prefix + "(" + parameters[0] + ", " + parameters[1] + ")";
        } else if (length == 3) {
            return prefix + "(" + parameters[0] + ", " + parameters[1] + ", " + parameters[2] + ")";
        }

        StringBuilder builder = new StringBuilder(32);
        builder.append(prefix);
        builder.append('(');
        builder.append(parameters[0]);
        for (int i = 1; i < length; i++) {
            builder.append(", ");
            builder.append(parameters[i]);
        }
        builder.append(')');
        return builder.toString();
    }

}
