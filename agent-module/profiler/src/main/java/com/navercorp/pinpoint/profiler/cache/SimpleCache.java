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

package com.navercorp.pinpoint.profiler.cache;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author emeroad
 */
public class SimpleCache<K, V> implements Cache<K, Result<V>> {
    // zero means not exist.
    private final ConcurrentMap<K, V> cache;
    protected final Function<K, V> idFunction;

    public static <K> SimpleCache<K, Integer> newIdCache() {
        return newIdCache(1024);
    }

    public static <K> SimpleCache<K, Integer> newIdCache(int cacheSize) {
        return new SimpleCache<>(cacheSize, new AtomicIntId<>());
    }

    public SimpleCache(Function<K, V> idFunction) {
        this(1024, idFunction);
    }

    public SimpleCache(int cacheSize, Function<K, V> idFunction) {
        this(cacheSize, -1, idFunction);
    }

    public SimpleCache(int cacheSize, long expireAfterWriteHours, Function<K, V> idFunction) {
        this.cache = createCache(cacheSize, expireAfterWriteHours);
        this.idFunction = Objects.requireNonNull(idFunction, "idFunction");
    }

    private ConcurrentMap<K, V> createCache(int maxCacheSize, long expireAfterWriteHours) {
        final Caffeine<Object, Object> cacheBuilder = CaffeineBuilder.newBuilder();
        cacheBuilder.initialCapacity(maxCacheSize);
        cacheBuilder.maximumSize(maxCacheSize);
        if (expireAfterWriteHours > 0) {
            cacheBuilder.expireAfterWrite(expireAfterWriteHours, TimeUnit.HOURS);
        }
        com.github.benmanes.caffeine.cache.Cache<K, V> localCache = cacheBuilder.build();
        return localCache.asMap();
    }

    @Override
    public Result<V> put(K value) {
        final PutIfAbsent putIfAbsent = new PutIfAbsent();
        final V id = this.cache.computeIfAbsent(value, putIfAbsent);
        return new Result<>(putIfAbsent.called, id);
    }

    private class PutIfAbsent implements Function<K, V> {

        private boolean called;

        public PutIfAbsent() {
        }

        @Override
        public V apply(K key) {
            this.called = true;
            return idFunction.apply(key);
        }
    }
}
