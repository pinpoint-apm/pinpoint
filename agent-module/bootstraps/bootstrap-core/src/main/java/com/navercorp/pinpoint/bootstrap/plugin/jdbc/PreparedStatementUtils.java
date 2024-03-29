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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author emeroad
 */
public final class PreparedStatementUtils {

    private static final Pattern BIND_SETTER = Pattern.compile("set[A-Z]([a-zA-Z]+)");

    private static final List<Method> bindMethod = findBindVariableSetMethod0();

    private PreparedStatementUtils() {
    }


    public static List<Method> findBindVariableSetMethod() {
        return Collections.unmodifiableList(bindMethod);
    }

    public static List<Method> findBindVariableSetMethod(BindVariableFilter filter) {
        Objects.requireNonNull(filter, "filter");

        List<Method> temp = new ArrayList<>(bindMethod.size());
        for (Method method : bindMethod) {
            if (filter.filter(method)) {
                temp.add(method);
            }
        }
        return temp;
    }


    static List<Method> findBindVariableSetMethod0() {
        if (!SqlModule.isSqlModuleEnable()) {
            return Collections.emptyList();
        }
        final Class<?> preparedStatement = SqlModule.getSqlPreparedStatement();
        Method[] methods = preparedStatement.getDeclaredMethods();
        List<Method> bindMethod = new ArrayList<>();
        for (Method method : methods) {
            if (isSetter(method.getName())) {
                if (method.getParameterCount() < 2) {
                    continue;
                }

                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes[0] != int.class) {
                    continue;
                }
                if (method.getReturnType() != void.class) {
                    continue;
                }
                if (!throwSqlException(method)) {
                    continue;
                }
                bindMethod.add(method);
            }
        }
        return Collections.unmodifiableList(bindMethod);
    }

    private static boolean throwSqlException(Method method) {
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        if (exceptionTypes.length == 1) {
            Class<?> exceptionType = exceptionTypes[0];
            if (exceptionType.getName().equals("java.sql.SQLException")) {
                return true;
            }
        }
        return false;
    }


    public static boolean isSetter(String name) {
        if (name == null) {
            return false;
        }
        return BIND_SETTER.matcher(name).matches();
    }
}
