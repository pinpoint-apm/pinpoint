/*
 * Copyright 2014 NAVER Corp.
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

import java.lang.reflect.Method;

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Jongho Moon
 *
 */
public class GetterAnalyzer {
    public GetterDetails analyze(Class<?> getterType) {
        Assert.requireNonNull(getterType, "getterType must not be null");
        
        if (!getterType.isInterface()) {
            throw new IllegalArgumentException("getterType " + getterType + "is not an interface");
        }
        
        Method[] methods = getterType.getDeclaredMethods();
        
        if (methods.length != 1) {
            throw new IllegalArgumentException("Getter interface must have only one method: " + getterType.getName());
        }
        
        Method getter = methods[0];
        
        if (getter.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("Getter interface method must be no-args and non-void: " + getterType.getName());
        }
        
        Class<?> fieldType = getter.getReturnType();
        
        if (fieldType == void.class || fieldType == Void.class) {
            throw new IllegalArgumentException("Getter interface method must be no-args and non-void: " + getterType.getName());
        }
        
        return new GetterDetails(getter, fieldType);
    }

    public static final class GetterDetails {
        private final Method getter;
        private final Class<?> fieldType;

        public GetterDetails(Method getter, Class<?> fieldType) {
            this.getter = getter;
            this.fieldType = fieldType;
        }

        public Method getGetter() {
            return getter;
        }

        public Class<?> getFieldType() {
            return fieldType;
        }
    }
}
