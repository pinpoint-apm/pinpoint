package com.navercorp.pinpoint.common.server.cache;

import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.cache.support.NullValue;

import java.time.Duration;
import java.util.function.Predicate;

public class NullValueExpiry<K, V> implements Expiry<K, V> {

    private static final long DISABLED = -1;
    private static final long NO_EXPIRATION = Long.MAX_VALUE;

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
        this.expireAfterWriteNanos = toNanos(expireAfterWrite);
        this.expireAfterAccessNanos = toNanos(expireAfterAccess);
        this.nullValueExpireAfterWriteNanos = toNanos(nullValueExpireAfterWrite);
        this.nullValuePredicate = nullValuePredicate;
    }

    private long toNanos(Duration duration) {
        if (duration == null || duration.isNegative()) {
            return DISABLED;
        }
        return duration.toNanos();
    }

    private boolean isEnabled(long durationNanos) {
        return durationNanos != DISABLED;
    }

    @Override
    public long expireAfterCreate(@NonNull K key, @NonNull V value, long currentTime) {
        if (nullValuePredicate.test(value) && isEnabled(nullValueExpireAfterWriteNanos)) {
            return nullValueExpireAfterWriteNanos;
        }
        if (isEnabled(expireAfterWriteNanos)) {
            return expireAfterWriteNanos;
        }
        return NO_EXPIRATION;
    }

    @Override
    public long expireAfterUpdate(@NonNull K key, @NonNull V value, long currentTime, @NonNegative long currentDuration) {
        if (nullValuePredicate.test(value) && isEnabled(nullValueExpireAfterWriteNanos)) {
            return nullValueExpireAfterWriteNanos;
        }
        if (isEnabled(expireAfterWriteNanos)) {
            return expireAfterWriteNanos;
        }
        return currentDuration;
    }

    @Override
    public long expireAfterRead(@NonNull K key, @NonNull V value, long currentTime, @NonNegative long currentDuration) {
        if (isEnabled(expireAfterAccessNanos)) {
            return expireAfterAccessNanos;
        }
        return currentDuration;
    }
}