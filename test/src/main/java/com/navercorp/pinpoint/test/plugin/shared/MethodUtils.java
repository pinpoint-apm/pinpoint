/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Taejin Koo
 */
public final class MethodUtils {

    private static final String PREFIX_GET = "get";
    private static final String PREFIX_IS = "is";
    private static final String PREFIX_SET = "set";

    private static Map<String, Class> primitiveTypeMap = null;

    private static Map<String, Class> getPrimitiveTypeMap() {
        if (primitiveTypeMap == null) {
            primitiveTypeMap = new HashMap<>();
            primitiveTypeMap.put("int", Integer.class);
            primitiveTypeMap.put("long", Long.class);
            primitiveTypeMap.put("double", Double.class);
            primitiveTypeMap.put("float", Float.class);
            primitiveTypeMap.put("bool", Boolean.class);
            primitiveTypeMap.put("boolean", Boolean.class);
            primitiveTypeMap.put("short", Short.class);
        }
        return primitiveTypeMap;
    }

    private MethodUtils() {
    }

    static MethodFilter createBeforeSharedMethodFilter() {
        return new MethodFilter.AndMethodFilter(new MethodFilter.AnnotationFilter(BeforeSharedClass.class),
                new MethodFilter.StaticFilter(), new MethodFilter.ParameterSizeFilter(0));
    }

    static MethodFilter createGetterMethodFilter() {
        MethodFilter.OrMethodFilter prefixMethod = new MethodFilter.OrMethodFilter(new MethodFilter.NamePrefixFilter(PREFIX_GET),
                new MethodFilter.NamePrefixFilter(PREFIX_IS));
        return new MethodFilter.AndMethodFilter(prefixMethod,
                new MethodFilter.StaticFilter(), new MethodFilter.StaticFilter(), new MethodFilter.ParameterSizeFilter(0));
    }

    static MethodFilter createSetterMethodFilter() {
        return new MethodFilter.AndMethodFilter(new MethodFilter.OrMethodFilter(new MethodFilter.NamePrefixFilter(PREFIX_SET)),
                new MethodFilter.StaticFilter(), new MethodFilter.ParameterSizeFilter(1));
    }

    static MethodFilter createAfterSharedMethodFilter() {
        return new MethodFilter.AndMethodFilter(new MethodFilter.AnnotationFilter(AfterSharedClass.class),
                new MethodFilter.StaticFilter(), new MethodFilter.ParameterSizeFilter(0));
    }

    static List<Method> getMethod(Class clazz, MethodFilter filter) {
        Objects.requireNonNull(clazz, "clazz");

        List<Method> result = new ArrayList<>();

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (filter.matches(method)) {
                result.add(method);
            }
        }

        return result;
    }

    static Map<String, Object> invokeGetMethod(Class clazz) throws Exception {
        Objects.requireNonNull(clazz, "clazz");

        Map<String, Object> properties = new HashMap<>();

        MethodFilter getterMethodFilter = createGetterMethodFilter();
        List<Method> getterMethods = MethodUtils.getMethod(clazz, getterMethodFilter);

        for (Method getterMethod : getterMethods) {
            Object invoke = getterMethod.invoke(null);
            String name = getterMethod.getName();

            if (name.startsWith(PREFIX_GET)) {
                properties.put(name.substring(PREFIX_GET.length()), invoke);
            } else if (name.startsWith(PREFIX_IS)) {
                properties.put(name.substring(PREFIX_IS.length()), invoke);
            }
        }

        return properties;
    }

    static void invokeSetMethod(Class clazz, Properties properties) throws InvocationTargetException, IllegalAccessException {
        Objects.requireNonNull(clazz, "clazz");

        MethodFilter setterMethodFilter = createSetterMethodFilter();
        List<Method> setterMethods = MethodUtils.getMethod(clazz, setterMethodFilter);

        for (Method setterMethod : setterMethods) {
            String methodName = setterMethod.getName();

            final String propertyName = methodName.substring(PREFIX_SET.length());
            Object propertyValue = properties.get(propertyName);
            if (propertyValue != null) {
                Type[] genericParameterTypes = setterMethod.getGenericParameterTypes();

                Type genericParameterType = genericParameterTypes[0];

                boolean canInvoke = ((Class<?>) (genericParameterType)).isInstance(propertyValue);
                if (canInvoke) {
                    setterMethod.invoke(null, propertyValue);
                } else if (genericParameterType != null) {
                    String parameterTypeName = genericParameterType.toString();
                    Class parameterTypeClazz = getPrimitiveTypeMap().get(parameterTypeName);

                    if (parameterTypeClazz != null && parameterTypeClazz.isInstance(propertyValue)) {
                        setterMethod.invoke(null, propertyValue);
                    }
                }
            }
        }
    }

    static void invokeStaticAndNoParametersMethod(List<Method> methods) throws Exception {
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (!Modifier.isStatic(modifiers)) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes != null && parameterTypes.length > 0) {
                continue;
            }

            method.invoke(null, null);
        }
    }

}
