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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

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

        this.caches = CacheBuilder.newBuilder()
                .maximumSize(this.cacheSize)
                .initialCapacity(this.cacheSize)
                .concurrencyLevel(4)
                .build(new CacheLoader<String, Hierarchy>() {
                    @Override
                    public Hierarchy load(String s) throws Exception {
                        return new Hierarchy();
                    }
                });
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
        try {
            return this.caches.get(key).cache.getIfPresent(classInternalName) != null;
        } catch (ExecutionException ignored) {
        }

        return false;
    }

    @Override
    public void put(String key, String classInternalName) {
        try {
            this.caches.get(key).cache.put(classInternalName, Boolean.TRUE);
        } catch (ExecutionException ignored) {
        }
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
            cache = CacheBuilder.newBuilder().maximumSize(cacheEntrySize).initialCapacity(cacheEntrySize).concurrencyLevel(4).build();
        }

        @Override
        public String toString() {
            return cache.asMap().keySet().toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("caches=").append(caches.asMap()).append(", ");
        sb.append("size=").append(caches.size()).append(", ");
        sb.append("stats=").append(caches.stats());
        sb.append("}");
        return sb.toString();
    }
}
