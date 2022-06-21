/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;


import java.util.concurrent.ExecutionException;

/**
 * @author jaehong.kim
 */
public class DefaultHierarchyCaches implements HierarchyCaches {
    private static final int MAX = 64;

    private final LoadingCache<String, Hierarchy> caches;
    private final int cacheSize;
    private final int cacheEntrySize;

    public DefaultHierarchyCaches(final int size, final int entrySize) {
        if (size <= 0) {
            throw new IllegalArgumentException("negative cache size:" + size);
        }

        this.cacheSize = getCacheSize(size);

        this.cacheEntrySize = getCacheEntrySize(entrySize);

        this.caches = Caffeine.newBuilder()
                .maximumSize(this.cacheSize)
                .initialCapacity(this.cacheSize)
                .build(this::loadEntry);
    }

    private Hierarchy loadEntry(String key) {
        return new Hierarchy();
    }

    private int getCacheEntrySize(int entrySize) {
        if (entrySize <= 0) {
            // check mistake.
            return this.cacheSize;
        } else if (entrySize > MAX) {
            return MAX;
        }
        return entrySize;
    }

    private int getCacheSize(int size) {
        if (size > MAX) {
            return MAX;
        }
        return size;
    }

    @Override
    public boolean get(String key, String classInternalName) {

        Hierarchy hierarchy = this.caches.get(key);
        return hierarchy.cache.getIfPresent(classInternalName) != null;
    }

    @Override
    public void put(String key, String classInternalName) {
        Hierarchy hierarchy = this.caches.get(key);
        hierarchy.cache.put(classInternalName, Boolean.TRUE);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    class Hierarchy {
        Cache<String, Boolean> cache;

        public Hierarchy() {
            cache = Caffeine.newBuilder()
                    .maximumSize(cacheEntrySize)
                    .initialCapacity(cacheEntrySize)
                    .build();
        }

        @Override
        public String toString() {
            return cache.asMap().keySet().toString();
        }
    }

    @Override
    public String toString() {
        String sb = "{" +
                "stats=" + caches.stats() +
                "}";
        return sb;
    }
}
