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

package com.navercorp.pinpoint.profiler.metadata;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class SimpleCache<T> {

    // zero means not exist.
    private final AtomicInteger idGen;
    private final ConcurrentMap<T, Result> cache;
    private final IdTransformer idTransformer;

    public SimpleCache(IdTransformer idTransformer) {
        this(idTransformer, 1024, 1);
    }

    public SimpleCache(IdTransformer idTransformer, int cacheSize) {
        this(idTransformer, cacheSize, 1);
    }

    public SimpleCache(IdTransformer idTransformer, int cacheSize, int startValue) {
        this.idGen = new AtomicInteger(startValue);
        this.cache = createCache(cacheSize);
        this.idTransformer = Assert.requireNonNull(idTransformer, "idTransformer");

    }

    private ConcurrentMap<T, Result> createCache(int maxCacheSize) {
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        cacheBuilder.concurrencyLevel(64);
        cacheBuilder.initialCapacity(maxCacheSize);
        cacheBuilder.maximumSize(maxCacheSize);
        Cache<T, Result> localCache = cacheBuilder.build();
        ConcurrentMap<T, Result> cache = localCache.asMap();
        return cache;
    }

    public Result put(T value) {
        final Result find = this.cache.get(value);
        if (find != null) {
            return find;
        }
        
        // Use negative values too to reduce data size
        final int newId = nextId();
        final Result result = new Result(false, newId);
        final Result before = this.cache.putIfAbsent(value, result);
        if (before != null) {
            return before;
        }
        return new Result(true, newId);
    }

    private int nextId() {
        int nextId = idGen.getAndIncrement();
        return this.idTransformer.transform(nextId);
    }

    public interface IdTransformer {
        int transform(int id);
    }

    public static class ZigZagTransformer implements IdTransformer  {
        @Override
        public int transform(int id) {
            return BytesUtils.zigzagToInt(id);
        }
    }

    public static class BypassTransformer implements IdTransformer {
        @Override
        public int transform(int id) {
            return id;
        }
    }


}
