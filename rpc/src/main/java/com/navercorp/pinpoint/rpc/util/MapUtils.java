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

package com.navercorp.pinpoint.rpc.util;

import java.util.Map;


/**
 * @author koo.taejin
 */
public final class MapUtils {

    private MapUtils() {
    }

    public static String getString(Map<Object, Object> map, String key) {
        return getString(map, key, null);
    }

    public static String getString(Map<Object, Object> map, String key, String defaultValue) {
        if (map == null) {
            return defaultValue;
        }

        final Object value = map.get(key);
        if (value instanceof String) {
            return (String) value;
        }

        return defaultValue;
    }

    public static Boolean getBoolean(Map<Object, Object> map, String key) {
        return getBoolean(map, key, false);
    }

    public static Boolean getBoolean(Map<Object, Object> map, String key, Boolean defaultValue) {
        if (map == null) {
            return defaultValue;
        }

        final Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        return defaultValue;
    }


    public static Integer getInteger(Map<Object, Object> map, String key) {
        return getInteger(map, key, null);
    }

    public static Integer getInteger(Map<Object, Object> map, String key, Integer defaultValue) {
        if (map == null) {
            return defaultValue;
        }

        final Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }

        return defaultValue;
    }
    
    public static Long getLong(Map<Object, Object> map, String key) {
        return getLong(map, key, null);
    }

    public static Long getLong(Map<Object, Object> map, String key, Long defaultValue) {
        if (map == null) {
            return defaultValue;
        }

        final Object value = map.get(key);
        if (value instanceof Long) {
            return (Long) value;
        }

        return defaultValue;
    }

}
