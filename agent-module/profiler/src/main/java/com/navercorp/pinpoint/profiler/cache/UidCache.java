package com.navercorp.pinpoint.profiler.cache;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class UidCache implements Cache<String, Result<byte[]>> {
    // zero means not exist.
    private final ConcurrentMap<String, Result<byte[]>> cache;

    private final Function<String, byte[]> uidFunction;

    public UidCache(int cacheSize, Function<String, byte[]> uidFunction) {
        this.cache = createCache(cacheSize);
        this.uidFunction = uidFunction;
    }

    private ConcurrentMap<String, Result<byte[]>> createCache(int maxCacheSize) {
        final Caffeine<Object, Object> cacheBuilder = CaffeineBuilder.newBuilder();
        cacheBuilder.initialCapacity(maxCacheSize);
        cacheBuilder.maximumSize(maxCacheSize);
        com.github.benmanes.caffeine.cache.Cache<String, Result<byte[]>> localCache = cacheBuilder.build();
        return localCache.asMap();
    }

    @Override
    public Result<byte[]> put(String value) {
        final Result<byte[]> find = this.cache.get(value);
        if (find != null) {
            return find;
        }

        final byte[] uid = uidFunction.apply(value);
        final Result<byte[]> result = new Result<>(false, uid);
        final Result<byte[]> before = this.cache.putIfAbsent(value, result);
        if (before != null) {
            return before;
        }
        return new Result<>(true, uid);
    }
}
