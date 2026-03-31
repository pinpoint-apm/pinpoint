package com.navercorp.pinpoint.common.server.cache;

import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.cache.support.NullValue;

import java.time.Duration;
import java.util.function.Predicate;

public class NullValueExpiry<K, V> implements Expiry<K, V> {

    private final long expireAfterWriteNanos;
    private final long expireAfterAccessNanos;
    private final long nullValueExpireAfterWriteNanos;
    private final Predicate<V> nullValuePredicate;

    public NullValueExpiry(Duration expireAfterWrite, Duration expireAfterAccess, Duration nullValueExpireAfterWrite) {
        this(expireAfterWrite, expireAfterAccess, nullValueExpireAfterWrite,
                NullValue.INSTANCE::equals);
    }

    public NullValueExpiry(Duration expireAfterWrite, Duration expireAfterAccess, Duration nullValueExpireAfterWrite,
                           Predicate<V> nullValuePredicate) {
        this.expireAfterWriteNanos = toExpiryNanos(expireAfterWrite);
        this.expireAfterAccessNanos = toExpiryNanos(expireAfterAccess);
        this.nullValueExpireAfterWriteNanos = toExpiryNanos(nullValueExpireAfterWrite);
        this.nullValuePredicate = nullValuePredicate;
    }

    private long toExpiryNanos(Duration duration) {
        if (duration == null || duration.isNegative()) {
            return Long.MAX_VALUE;
        }
        return duration.toNanos();
    }

    @Override
    public long expireAfterCreate(@NonNull K key, @NonNull V value, long currentTime) {
        if (nullValuePredicate.test(value)) {
            return nullValueExpireAfterWriteNanos;
        }
        return expireAfterWriteNanos;
    }

    @Override
    public long expireAfterUpdate(@NonNull K key, @NonNull V value, long currentTime, @NonNegative long currentDuration) {
        if (nullValuePredicate.test(value)) {
            return nullValueExpireAfterWriteNanos;
        }
        return expireAfterWriteNanos;
    }

    @Override
    public long expireAfterRead(@NonNull K key, @NonNull V value, long currentTime, @NonNegative long currentDuration) {
        return expireAfterAccessNanos;
    }
}