package com.nhn.pinpoint.profiler.metadata;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ConcurrentMap;


/**
 * concurrent lru cache
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
