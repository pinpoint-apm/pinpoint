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
import com.navercorp.pinpoint.common.util.apache.IntHashMapUtils;
import com.navercorp.pinpoint.io.header.Header;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TypeLocatorBuilder<T> {

    private final Map<Integer, BodyFactory<T>> bodyFactoryMap = new HashMap<Integer, BodyFactory<T>>();
    private final Map<Class<?>, Header> bodyClassToHeaderMap = new LinkedHashMap<Class<?>, Header>();

    //    private final IntHashMap<Class<?>> classMap = new IntHashMap<Class<?>>();
    private final Map<Integer, Header> headerMap = new HashMap<Integer, Header>();
    private final HeaderFactory headerFactory;

    public TypeLocatorBuilder() {
        this(new HeaderFactoryV1());
    }

    public TypeLocatorBuilder(HeaderFactory headerFactory) {
        this.headerFactory = Assert.requireNonNull(headerFactory, "headerFactory");
    }

    public void addBodyFactory(short type, BodyFactory<T> bodyFactory) {
        if (bodyFactory == null) {
            throw new NullPointerException("bodyFactory");
        }

        final BodyFactory<T> old = bodyFactoryMap.put((int) type, bodyFactory);
        if (old != null) {
            throw new IllegalStateException("duplicated type:" + type);
        }
        final Header header = this.headerFactory.newHeader(type);
        final Header oldTypeHeader = headerMap.put((int) type, header);
        if (oldTypeHeader != null) {
            throw new IllegalStateException("duplicated type:" + type);
        }

        final T object = bodyFactory.getObject();
        if (object != null) {
            final Class<?> bodyClass = object.getClass();
            //        classMap.put(type, bodyClass);
            final Header oldBodyClass = bodyClassToHeaderMap.put(bodyClass, header);
            if (oldBodyClass != null) {
                throw new IllegalStateException("duplicated type:" + type);
            }
        }
    }

    public TypeLocator<T> build() {
        final IntHashMap<BodyFactory<T>> copyBodyFactoryMap = IntHashMapUtils.copy(bodyFactoryMap);
        final Map<Class<?>, Header> copyBodyClassToHeaderMap = new IdentityHashMap<Class<?>, Header>(this.bodyClassToHeaderMap);
        final IntHashMap<Header> copyHeaderMap = IntHashMapUtils.copy(headerMap);
        final List<DefaultTypeLocator.Entry<Class<?>, Header>> bodyClassToHeaderList = toList(this.bodyClassToHeaderMap);

        TypeLocator typeLocator = new DefaultTypeLocator<T>(copyBodyFactoryMap, copyBodyClassToHeaderMap, copyHeaderMap, bodyClassToHeaderList);
        return typeLocator;
    }

    private List<DefaultTypeLocator.Entry<Class<?>, Header>> toList(Map<Class<?>, Header> bodyClassToHeaderMap) {
        List<DefaultTypeLocator.Entry<Class<?>, Header>> bodyClassToHeaderList = new ArrayList<DefaultTypeLocator.Entry<Class<?>, Header>>(bodyClassToHeaderMap.size());
        for (Map.Entry<Class<?>, Header> mapEntry : bodyClassToHeaderMap.entrySet()) {
            DefaultTypeLocator.Entry<Class<?>, Header> entry = new DefaultTypeLocator.Entry<Class<?>, Header>(mapEntry.getKey(), mapEntry.getValue());
            bodyClassToHeaderList.add(entry);
        }
        return bodyClassToHeaderList;
    }


}
