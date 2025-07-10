package com.navercorp.pinpoint.common.server.uid.cache;

import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.cache.support.NullValue;

import java.time.Duration;

public class NullValueExpiry implements Expiry<String, Object> {

    private final long expireAfterWriteNanos;
    private final long expireAfterAccessNanos;
    private final long nullExpireAfterWriteNanos;

    public NullValueExpiry(Duration expireAfterWrite, Duration expireAfterAccess, Duration nullExpireAfterWrite) {
        this.expireAfterWriteNanos = toExpiryNanos(expireAfterWrite);
        this.expireAfterAccessNanos = toExpiryNanos(expireAfterAccess);
        this.nullExpireAfterWriteNanos = toExpiryNanos(nullExpireAfterWrite);
    }

    private long toExpiryNanos(Duration duration) {
        if (duration == null || duration.isNegative()) {
            return Long.MAX_VALUE;
        }
        return duration.toNanos();
    }

    @Override
    public long expireAfterCreate(@NonNull String key, @NonNull Object value, long currentTime) {
        if (NullValue.INSTANCE.equals(value)) {
            return nullExpireAfterWriteNanos;
        }
        return expireAfterWriteNanos;
    }

    @Override
    public long expireAfterUpdate(@NonNull String key, @NonNull Object value, long currentTime, @NonNegative long currentDuration) {
        if (NullValue.INSTANCE.equals(value)) {
            return nullExpireAfterWriteNanos;
        }
        return expireAfterWriteNanos;
    }

    @Override
    public long expireAfterRead(@NonNull String key, @NonNull Object value, long currentTime, @NonNegative long currentDuration) {
        return expireAfterAccessNanos;
    }
}
