/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.context.scope;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ConcurrentPool<K, V> implements Pool<K, V> {

    private final ConcurrentMap<K, V> pool = new ConcurrentHashMap<K, V>();

    private final PoolObjectFactory<K, V> objectFactory;

    public ConcurrentPool(PoolObjectFactory<K, V> objectFactory) {
        this.objectFactory = Assert.requireNonNull(objectFactory, "objectFactory");
    }

    @Override
    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        final V alreadyExist = this.pool.get(key);
        if (alreadyExist != null) {
            return alreadyExist;
        }

        final V newValue = this.objectFactory.create(key);
        final V oldValue = this.pool.putIfAbsent(key, newValue);
        if (oldValue != null) {
            return oldValue;
        }
        return newValue;
    }


}
