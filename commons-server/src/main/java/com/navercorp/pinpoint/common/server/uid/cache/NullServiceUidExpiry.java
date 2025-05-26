package com.navercorp.pinpoint.common.server.uid.cache;

import com.github.benmanes.caffeine.cache.Expiry;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.time.Duration;

public class NullServiceUidExpiry implements Expiry<String, ServiceUid> {

    private final long expireAfterWriteNanos;
    private final long expireAfterAccessNanos;
    private final long nullExpireAfterWriteNanos;

    public NullServiceUidExpiry(Duration expireAfterWrite, Duration expireAfterAccess, Duration nullExpireAfterWrite) {
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
    public long expireAfterCreate(@NonNull String key, @NonNull ServiceUid value, long currentTime) {
        if (value.equals(ServiceUid.NULL)) {
            return nullExpireAfterWriteNanos;
        }
        return expireAfterWriteNanos;
    }

    @Override
    public long expireAfterUpdate(@NonNull String key, @NonNull ServiceUid value, long currentTime, @NonNegative long currentDuration) {
        if (value.equals(ServiceUid.NULL)) {
            return nullExpireAfterWriteNanos;
        }
        return expireAfterWriteNanos;
    }

    @Override
    public long expireAfterRead(@NonNull String key, @NonNull ServiceUid value, long currentTime, @NonNegative long currentDuration) {
        return expireAfterAccessNanos;
    }
}
