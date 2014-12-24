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

package com.navercorp.pinpoint.thrift.io;

/**
 * @author emeroad
 */
public class ThreadLocalHeaderTBaseDeserializerFactory<E> implements DeserializerFactory<E> {

    private final ThreadLocal<E> cache = new ThreadLocal<E>() {
        @Override
        protected E initialValue() {
            return factory.createDeserializer();
        }
    };

    private final DeserializerFactory<E> factory;

    public ThreadLocalHeaderTBaseDeserializerFactory(DeserializerFactory<E> factory) {
        if (factory == null) {
            throw new NullPointerException("factory must not be null");
        }
        this.factory = factory;
    }

    @Override
    public E createDeserializer() {
        return cache.get();
    }
}
