/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.io.util;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.io.header.Header;

import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTypeLocator<T> implements TypeLocator<T> {


    private final IntHashMap<BodyFactory<T>> bodyFactoryMap;
    private final Map<Class<?>, Header> bodyToHeaderMap;
    private final DefaultTypeLocator.Entry<Class<?>, Header>[] bodyClassToHeaderArray;
    private final IntHashMap<Header> headerMap;

    DefaultTypeLocator(IntHashMap<BodyFactory<T>> bodyFactoryMap, Map<Class<?>, Header> bodyToHeaderMap,
                       IntHashMap<Header> headerMap , List<Entry<Class<?>, Header>> bodyClassToHeaderList) {
        this.bodyFactoryMap = Assert.requireNonNull(bodyFactoryMap, "bodyFactoryMap");
        this.bodyToHeaderMap = Assert.requireNonNull(bodyToHeaderMap, "bodyToHeaderMap");

        this.headerMap = Assert.requireNonNull(headerMap , "headerMap ");

        Assert.requireNonNull(bodyClassToHeaderList, "bodyClassToHeaderList");
        this.bodyClassToHeaderArray = bodyClassToHeaderList.toArray(new Entry[0]);
    }

    @Override
    public T bodyLookup(short type) {
        final BodyFactory<T> bodyFactory = this.bodyFactoryMap.get(type);
        if (bodyFactory != null) {
            return bodyFactory.getObject();
        }
        return null;
    }

    @Override
    public Header headerLookup(T body) {
        if (body == null) {
            throw new IllegalArgumentException("body");
        }

        for (Entry<Class<?>, Header> entry : bodyClassToHeaderArray) {
            final Class<?> bodyClass = entry.key;
            if (bodyClass.isInstance(body)) {
                return entry.value;
            }
        }

        return null;
    }

    @Override
    public Header headerLookup(short type) {
        return this.headerMap.get(type);
    }

    @Override
    public boolean isSupport(short type) {
        final BodyFactory<T> bodyFactory = this.bodyFactoryMap.get(type);
        if (bodyFactory != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isSupport(Class<? extends T> clazz) {
        final Header header = bodyToHeaderMap.get(clazz);
        if (header != null) {
            return true;
        }
        return false;
    }

    static class Entry<K, V> {
        private final K key;
        private final V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
