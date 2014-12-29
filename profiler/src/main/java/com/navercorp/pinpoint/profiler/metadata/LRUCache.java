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

package com.navercorp.pinpoint.profiler.metadata;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ConcurrentMap;


/**
 * Concurrent LRU cache
 * @author emeroad
 */
public class LRUCache<T> {

    private static final Object V = new Object();
    public static final int DEFAULT_CACHE_SIZE = 1024;

    private final ConcurrentMap<T, Object> cache;


    public LRUCache(int maxCacheSize) {
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        cacheBuilder.concurrencyLevel(32);
        cacheBuilder.initialCapacity(maxCacheSize);
        cacheBuilder.maximumSize(maxCacheSize);
        Cache<T, Object> localCache = cacheBuilder.build();
        this.cache = localCache.asMap();
    }

    public LRUCache() {
        this(DEFAULT_CACHE_SIZE);
    }


    public boolean put(T value) {

        Object oldValue = cache.putIfAbsent(value, V);
        if (oldValue == null) {
            return true;
        }
        return false;

    }

    public long getSize() {
        return cache.size();
    }

}
