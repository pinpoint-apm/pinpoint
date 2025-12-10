package com.navercorp.pinpoint.profiler.cache;

public class UidCache extends SimpleCache<String, byte[]> {
    private final int bypassLength;

    public UidCache(int cacheSize, UidGenerator idFunction, int bypassLength) {
        super(cacheSize, idFunction);
        this.bypassLength = bypassLength;
    }

    public UidCache(int cacheSize, long expireAfterWriteHours, UidGenerator idFunction, int bypassLength) {
        super(cacheSize, expireAfterWriteHours, idFunction);
        this.bypassLength = bypassLength;
    }

    @Override
    public Result<byte[]> put(String key) {
        if (bypassLength == -1 || key.length() < bypassLength) {
            return super.put(key);
        } else {
            return new Result<>(true, idFunction.apply(key));
        }
    }
}
