package com.nhn.pinpoint.profiler.metadata;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nhn.pinpoint.common.util.BytesUtils;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class SimpleCache<T> {
    // 0인값은 존재 하지 않음을 나타냄.
    private final AtomicInteger idGen;
    private final ConcurrentMap<T, Result> cache;


    public SimpleCache() {
        this(1024, 1);
    }

    public SimpleCache(int cacheSize) {
        this(cacheSize, 1);
    }

    public SimpleCache(int cacheSize, int startValue) {
        idGen = new AtomicInteger(startValue);
        cache = createCache(cacheSize);
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
        //음수까지 활용하여 가능한 데이터 인코딩을 작게 유지되게 함.
        final int newId = BytesUtils.zigzagToInt(idGen.getAndIncrement());
        final Result result = new Result(false, newId);
        final Result before = this.cache.putIfAbsent(value, result);
        if (before != null) {
            return before;
        }
        return new Result(true, newId);
    }

}
