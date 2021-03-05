/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.instrument.transformer;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ArrayUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class ParameterUtils {

    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    private ParameterUtils() {
    }

    public static boolean hasNull(final Object[] parameters) {
        if (ArrayUtils.isEmpty(parameters)) {
            return false;
        }
        for (Object parameter : parameters) {
            if (parameter == null) {
                return true;
            }
        }
        return false;
    }

    public static Class<?>[] toClass(final Object[] parameters) {
        if (parameters == null) {
            return null;
        }
        if (parameters.length == 0) {
            return EMPTY_CLASS_ARRAY;
        }
        final Class<?>[] parameterTypeArray = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final Object argument = parameters[i];
            final Class<?> type = argument == null ? null : argument.getClass();
            parameterTypeArray[i] = type;
        }
        return parameterTypeArray;
    }


    private static final Class<?>[] SUPPORT_CLASS = {
            ServiceType.class,
            String.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
    };


    public static void checkParameterType(final Class[] classes) {
        if (ArrayUtils.isEmpty(classes)) {
            return;
        }
        for (Class<?> clazz: classes) {
            if (clazz == null) {
                continue;
            }
//            support all bootstrap classes?
//            if (clazz.getClassLoader() == Object.class.getClassLoader()) {
//                return;
//            }
            if (clazz.getClassLoader() != Object.class.getClassLoader()) {
                throw new IllegalArgumentException("unsupported classloader " + clazz);
            }

            clazz = getRawComponentType(clazz);
            if (clazz.isPrimitive()) {
                return;
            }
            boolean supportedClass = false;
            for (Class<?> supportClass : SUPPORT_CLASS) {
                if (supportClass.isAssignableFrom(clazz)) {
                    supportedClass = true;
                    break;
                }
            }
            if (!supportedClass) {
                throw new IllegalArgumentException("unsupported type:" + clazz);
            }
        }
    }

    static Class<?> getRawComponentType(Class<?> aClass) {
        while (aClass.isArray()) {
            aClass = aClass.getComponentType();
        }
        return aClass;
    }

}
