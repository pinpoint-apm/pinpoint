package com.nhn.pinpoint.profiler.metadata;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ConcurrentMap;


/**
 * concurrent lru cache
 */
public class LRUCache<T> {

    private static final Object V = new Object();

    private final ConcurrentMap<T, Object> cache;

    public LRUCache(int maxCacheSize) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        cacheBuilder.concurrencyLevel(8);
        cacheBuilder.initialCapacity(maxCacheSize);
        cacheBuilder.maximumSize(maxCacheSize);
        Cache<T, Object> localCache = cacheBuilder.build();
        this.cache = localCache.asMap();
    }

    public LRUCache() {
        this(2048);
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
