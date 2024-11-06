package com.navercorp.pinpoint.profiler.cache;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class UidCache extends SimpleCache<String, byte[]> {
    private static final Function<String, byte[]> hashFunction = x -> Hashing.murmur3_128().hashString(x, StandardCharsets.UTF_8).asBytes();

    private final int bypassLength;

    public UidCache(int cacheSize, int bypassLength) {
        super(cacheSize, hashFunction);
        this.bypassLength = bypassLength;
    }

    @Override
    public Result<byte[]> put(String key) {
        if (bypassLength == -1 || key.length() < bypassLength) {
            return super.put(key);
        } else {
            return new Result<>(true, hashFunction.apply(key));
        }
    }
}
