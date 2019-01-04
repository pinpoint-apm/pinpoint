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


import java.util.List;

/**
 * @author Taejin Koo
 */
public final class ListUtils {

    private ListUtils() {
    }

    public static <V> boolean addIfValueNotNull(List<V> list, V value) {
        if (value == null) {
            return false;
        }

        return list.add(value);
    }

    public static <V> boolean addAllIfAllValuesNotNull(List<V> list, V[] values) {
        if (values == null) {
            return false;
        }

        for (V value : values) {
            if (value == null) {
                return false;
            }
        }

        for (V value : values) {
            list.add(value);
        }

        return true;
    }

    public static <V> void addAllExceptNullValue(List<V> list, V[] values) {
        if (values == null) {
            return;
        }

        for (V value : values) {
            addIfValueNotNull(list, value);
        }
    }

    public static <V> V getFirst(List<V> list) {
        return getFirst(list, null);
    }

    public static <V> V getFirst(List<V> list, V defaultValue) {
        if (list == null) {
            return defaultValue;
        }

        int size = list.size();
        if (size > 0) {
            return list.get(0);
        } else {
            return defaultValue;
        }
    }

    public static <V> boolean isFirst(List<V> list, V object) {
        V first = getFirst(list);
        if (first == null) {
            return object == null;
        }
        return first.equals(object);
    }

    public static <V> V get(List<V> list, int index, V defaultValue) {
        try {
            return list.get(index);
        } catch (Exception ignore) {
        }

        return defaultValue;
    }

    public static <V> V getLast(List<V> list) {
        return getLast(list, null);
    }

    public static <V> V getLast(List<V> list, V defaultValue) {
        if (list == null) {
            return defaultValue;
        }

        int size = list.size();
        if (size > 0) {
            return list.get(size - 1);
        } else {
            return defaultValue;
        }
    }

    public static <V> boolean isLast(List<V> list, V object) {
        V last = getLast(list);
        if (last == null) {
            return object == null;
        }
        return last.equals(object);
    }

}
