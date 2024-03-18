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

/**
 * @author emeroad
 */
public class SimpleCache<T> implements Cache<T, Result<Integer>> {
    // zero means not exist.
    private final ConcurrentMap<T, Result<Integer>> cache;
    private final IdAllocator idAllocator;

    public SimpleCache(IdAllocator idAllocator) {
        this(idAllocator, 1024);
    }

    public SimpleCache(IdAllocator idAllocator, int cacheSize) {
        this.cache = createCache(cacheSize);
        this.idAllocator = Objects.requireNonNull(idAllocator, "idTransformer");

    }

    private ConcurrentMap<T, Result<Integer>> createCache(int maxCacheSize) {
        final Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
        cacheBuilder.initialCapacity(maxCacheSize);
        cacheBuilder.maximumSize(maxCacheSize);
        com.github.benmanes.caffeine.cache.Cache<T, Result<Integer>> localCache = cacheBuilder.build();
        return localCache.asMap();
    }

    @Override
    public Result<Integer> put(T value) {
        final Result<Integer> find = this.cache.get(value);
        if (find != null) {
            return find;
        }

        // Use negative values too to reduce data size
        final int newId = nextId();
        final Result<Integer> result = new Result<>(false, newId);
        final Result<Integer> before = this.cache.putIfAbsent(value, result);
        if (before != null) {
            return before;
        }
        return new Result<>(true, newId);
    }

    private int nextId() {
        return this.idAllocator.allocate();
    }
}
