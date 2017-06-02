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
 */

package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.common.util.Assert;

import java.lang.reflect.Method;

/**
 * @author HyunGil Jeong
 */
public class SetterAnalyzer {

    public SetterDetails analyze(Class<?> setterType) {
        Assert.requireNonNull(setterType, "setterType must not be null");

        if (!setterType.isInterface()) {
            throw new IllegalArgumentException("setterType " + setterType + "is not an interface");
        }

        Method[] methods = setterType.getDeclaredMethods();

        if (methods.length != 1) {
            throw new IllegalArgumentException("Setter interface must have only one method: " + setterType.getName());
        }

        Method setter = methods[0];
        Class<?>[] arguments = setter.getParameterTypes();

        if (arguments.length != 1) {
            throw new IllegalArgumentException("Setter interface method must have exactly 1 argument: " + setterType.getName());
        }

        Class<?> fieldType = arguments[0];

        Class<?> returnType = setter.getReturnType();

        if (returnType != void.class) {
            throw new IllegalArgumentException("Setter must have return type void: " + setterType.getName());
        }

        return new SetterDetails(setter, fieldType);
    }

    public static final class SetterDetails {
        private final Method setter;
        private final Class<?> fieldType;

        public SetterDetails(Method setter, Class<?> fieldType) {
            this.setter = setter;
            this.fieldType = fieldType;
        }

        public Method getSetter() {
            return setter;
        }

        public Class<?> getFieldType() {
            return fieldType;
        }
    }
}
