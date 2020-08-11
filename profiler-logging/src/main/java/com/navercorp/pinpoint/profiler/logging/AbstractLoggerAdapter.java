/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.logging;

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.SqlModule;
import com.navercorp.pinpoint.common.util.ArrayUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class AbstractLoggerAdapter {
    public static final int BUFFER_SIZE = 256;

    protected enum TYPE {
        BEFORE, AFTER
    }

    private static Object EXIST = new Object();

    private static final Map<Class<?>, Object> SIMPLE_TYPE = prepare();

    private static Map<Class<?>, Object> prepare() {
        final Map<Class<?>, Object> map = new IdentityHashMap<Class<?>, Object>(64);
        put(map, String.class);
        put(map, Boolean.class);
        put(map, boolean.class);
        put(map, Byte.class);
        put(map, byte.class);
        put(map, Short.class);
        put(map, short.class);
        put(map, Integer.class);
        put(map, int.class);
        put(map, Long.class);
        put(map, long.class);
        put(map, Float.class);
        put(map, float.class);
        put(map, Double.class);
        put(map, double.class);
        put(map, Character.class);
        put(map, char.class);
        put(map, BigDecimal.class);
        put(map, StringBuffer.class);
        put(map, BigInteger.class);
        put(map, java.util.Date.class);
        put(map, Class.class);
        put(map, Calendar.class);
        put(map, GregorianCalendar.class);
        put(map, URL.class);
        put(map, Object.class);

        if (SqlModule.isSqlModuleEnable()) {
            addSqlModuleSupport(map);
        }
        return map;
    }

    private static void addSqlModuleSupport(Map<Class<?>, Object> map) {
        put(map, SqlModule.getSqlDate());
        put(map, SqlModule.getSqlTime());
        put(map, SqlModule.getSqlTimestamp());
    }

    private static void put(Map<Class<?>, Object> map, Class<?> key) {
        map.put(key, EXIST);
    }


    protected static void logResult(StringBuilder sb, Object result, Throwable throwable) {
        if (throwable == null) {
            sb.append(" result:");
            sb.append(normalizedParameter(result));
        } else {
            sb.append(" Caused:");
            sb.append(throwable.getMessage());
        }
    }


    protected static StringBuilder logMethod(TYPE type, Object target, String className, String methodName, String parameterDescription, Object[] args) {
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        sb.append(type.toString());
        sb.append(' ');
        sb.append(getTarget(target));
        sb.append(' ');
        sb.append(className);
        sb.append(' ');
        sb.append(methodName);
        sb.append(parameterDescription);
        sb.append(" args:");
        appendParameterList(sb, args);
        return sb;
    }

    protected static StringBuilder logMethod(TYPE type, Object target, Object[] args) {
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        sb.append(type.toString());
        sb.append(' ');
        sb.append(getTarget(target));
        sb.append(' ');
        sb.append(" args:");
        appendParameterList(sb, args);
        return sb;
    }

    private static String getTarget(Object target) {
        // Use class name instead of target.toString() because latter could cause side effects.
        if (target == null) {
            return "target=null";
        } else {
            return target.getClass().getName();
        }
    }

    private static void appendParameterList(StringBuilder sb, Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            sb.append("()");
            return;
        }
        if (args.length > 0) {
            sb.append('(');
            sb.append(normalizedParameter(args[0]));
            for (int i = 1; i < args.length; i++) {
                sb.append(", ");
                sb.append(normalizedParameter(args[i]));
            }
            sb.append(')');
        }
    }

    private static String normalizedParameter(Object arg) {
        // Do not call toString() because it could cause some side effects.
        if (arg == null) {
            return "null";
        } else {
            // Check if arg is simple type which is safe to invoke toString()
            if (isSimpleType(arg)) {
                return arg.toString();
            } else {
                return getSimpleName(arg.getClass());
            }
        }
    }

    static boolean isSimpleType(Object arg) {
        final Object find = SIMPLE_TYPE.get(arg.getClass());
        if (find == null) {
            return false;
        }
        return true;
    }

    static String getSimpleName(final Class<?> clazz) {
        if (clazz.isArray()) {
            return getSimpleName(clazz.getComponentType()) + "[]";
        }

        final String simpleName = clazz.getName();
        if (simpleName == null) {
            // Defense
            return "";
        }

        final int lastPackagePosition = simpleName.lastIndexOf('.');
        if (lastPackagePosition != -1) {
            // Strip the package name
            return simpleName.substring(lastPackagePosition + 1);
        }
        return simpleName;
    }

}
