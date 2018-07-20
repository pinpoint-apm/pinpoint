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
public class AccessorAnalyzer {
    public AccessorDetails analyze(Class<?> accessorType) {
        Assert.requireNonNull(accessorType, "accessorType");
        
        if (!accessorType.isInterface()) {
            throw new IllegalArgumentException("accessorType " + accessorType + "is not an interface");
        }
        
        Method[] methods = accessorType.getDeclaredMethods();
        
        if (methods.length != 2) {
            throw new IllegalArgumentException("accessorType has to declare 2 methods. " + accessorType + " has " + methods.length + ".");
        }
        
        Method getter;
        Method setter;
        
        if (methods[0].getParameterTypes().length == 0) {
            getter = methods[0];
            setter = methods[1];
        } else {
            getter = methods[1];
            setter = methods[0];
        }
        
        Class<?> fieldType = getter.getReturnType();
        
        if (fieldType == void.class || fieldType == Void.class) {
            throw new IllegalArgumentException("accessorType must declare an getter and setter: " + accessorType);
        }
        
        
        
        Class<?>[] setterParamTypes = setter.getParameterTypes();
        
        if (setterParamTypes.length != 1) {
            throw new IllegalArgumentException("accessorType must declare an getter and setter: " + accessorType);
        }
        
        if (setterParamTypes[0] != fieldType) {
            throw new IllegalArgumentException("The return type of getter is different to the parameter type of setter: " + accessorType);
        }
        
        if (setter.getReturnType() != void.class) {
            throw new IllegalArgumentException("Setter must have return type void: " + accessorType);
        }
        
        
        return new AccessorDetails(fieldType, getter, setter);
    }

    public static class AccessorDetails {
        private final Class<?> fieldType;
        private final Method getter;
        private final Method setter;
        
        public AccessorDetails(Class<?> fieldType, Method getter, Method setter) {
            this.fieldType = fieldType;
            this.getter = getter;
            this.setter = setter;
        }

        public Class<?> getFieldType() {
            return fieldType;
        }

        public Method getGetter() {
            return getter;
        }

        public Method getSetter() {
            return setter;
        }
        
    }
}
