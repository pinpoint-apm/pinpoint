package com.navercorp.pinpoint.profiler.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentMap;

public class UidCache implements Cache<String, Result<byte[]>> {

    // zero means not exist.
    private final ConcurrentMap<String, Result<byte[]>> cache;

    private final HashFunction hashFunction = Hashing.murmur3_128();

    public UidCache(int cacheSize) {
        this.cache = createCache(cacheSize);
    }

    private ConcurrentMap<String, Result<byte[]>> createCache(int maxCacheSize) {
        final Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
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

        final byte[] uid = calculateUid(value);
        final Result<byte[]> result = new Result<>(false, uid);
        final Result<byte[]> before = this.cache.putIfAbsent(value, result);
        if (before != null) {
            return before;
        }
        return new Result<>(true, uid);
    }

    private byte[] calculateUid(String value) {
            return hashFunction
                    .hashString(value, StandardCharsets.UTF_8)
                    .asBytes();
    }
}
