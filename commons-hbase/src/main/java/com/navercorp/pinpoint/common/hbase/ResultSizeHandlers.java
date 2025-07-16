/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;

public class ResultSizeHandlers {

    public static final ToIntFunction<?> NULL_HANDLER = value -> 0;

    public static final ToIntFunction<?> DEFAULT_HANDLER = value -> 1;

    public static final ToIntFunction<Collection<?>> COLLECTION_HANDLER = Collection::size;

    public static final ToIntFunction<Map<?, ?>> MAP_HANDLER = Map::size;

    public static final ToIntFunction<?> ARRAY_HANDLER = Array::getLength;

    @SuppressWarnings("unchecked")
    public static <T> ToIntFunction<T> getHandler(T t) {
        if (t == null) {
            return (ToIntFunction<T>) NULL_HANDLER;
        }
        if (t instanceof Collection<?>) {
            return (ToIntFunction<T>) COLLECTION_HANDLER;
        } else if (t instanceof Map<?, ?>) {
            return (ToIntFunction<T>) MAP_HANDLER;
        } else if (t.getClass().isArray()) {
            return (ToIntFunction<T>)  ARRAY_HANDLER;
        }
        return (ToIntFunction<T>) DEFAULT_HANDLER;
    }

    @SuppressWarnings("unchecked")
    public static <T> ToIntFunction<T> getHandler(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");

        if (Collection.class.isAssignableFrom(clazz)) {
            return (ToIntFunction<T>) COLLECTION_HANDLER;
        } else if (Map.class.isAssignableFrom(clazz)) {
            return (ToIntFunction<T>) MAP_HANDLER;
        } else if (clazz.isArray()) {
            return (ToIntFunction<T>) ARRAY_HANDLER;
        }
        return (ToIntFunction<T>) DEFAULT_HANDLER;
    }

}
