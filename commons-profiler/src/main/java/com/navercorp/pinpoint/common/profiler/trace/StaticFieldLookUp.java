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

package com.navercorp.pinpoint.common.profiler.trace;

import com.navercorp.pinpoint.common.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class StaticFieldLookUp<T> {

    public interface Filter<T>  {
        boolean FILTERED = true;
        boolean INCLUDE = false;
        boolean filter(T serviceType);
    }

    public static class BypassFilter<T> implements Filter<T> {
        @Override
        public boolean filter(T type) {
            return INCLUDE;
        }
    }

    public static class ExcludeFilter<T> implements Filter<T> {
        private final T[] excludeTypeList;

        public ExcludeFilter(T[] excludeTypeList) {
            this.excludeTypeList = Assert.requireNonNull(excludeTypeList, "excludeTypeList");
        }

        @Override
        public boolean filter(T type) {
            for (T excludeType : excludeTypeList) {
                if (excludeType == type) {
                    return FILTERED;
                }
            }
            return Filter.INCLUDE;
        }
    }

    private final Class<?> targetClazz;
    private final Class<T> findClazz;

    public StaticFieldLookUp(Class<?> targetClazz, Class<T> findClazz) {
        this.targetClazz = Assert.requireNonNull(targetClazz, "targetClazz");
        this.findClazz = Assert.requireNonNull(findClazz, "findClazz");
    }

    public List<T> lookup(Filter<T> filter) {
        if (filter == null) {
            throw new NullPointerException("filter");
        }
        final List<T> lookup = new ArrayList<T>();

        Field[] declaredFields = targetClazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            if (!field.getType().equals(findClazz)) {
                continue;
            }
            final Object filedObject = getObject(field);

            if (findClazz.isInstance(filedObject)) {
                T type = findClazz.cast(filedObject);
                if (filter.filter(type) == Filter.FILTERED) {
                    continue;
                }

                lookup.add(type);
            }
        }
        return lookup;
    }

    public List<T> lookup() {
        return lookup(new BypassFilter<T>());
    }

    private Object getObject(Field field) {
        try {
            return field.get(findClazz);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("service access fail. Caused by:" + ex.getMessage(), ex);
        }
    }



}
