package com.navercorp.pinpoint.common.server.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class NullValueExpiryTest {

    @Test
    void expireAfterCreateShouldUseExpireAfterWrite() {
        NullValueExpiry<String, String> expiry = new NullValueExpiry<>(
                Duration.ofMinutes(30), null, Duration.ofMinutes(1));

        long duration = expiry.expireAfterCreate("key", "value", 0);

        assertThat(duration).isEqualTo(Duration.ofMinutes(30).toNanos());
    }

    @Test
    void expireAfterReadShouldKeepCurrentDurationWhenExpireAfterAccessIsNotSet() {
        NullValueExpiry<String, String> expiry = new NullValueExpiry<>(
                Duration.ofMinutes(30), null, Duration.ofMinutes(1));

        long currentDuration = Duration.ofMinutes(10).toNanos();

        long duration = expiry.expireAfterRead("key", "value", 0, currentDuration);

        assertThat(duration).isEqualTo(currentDuration);
    }

    @Test
    void nullValueShouldUseNullValueExpireAfterWrite() {
        NullValueExpiry<String, String> expiry = new NullValueExpiry<>(
                Duration.ofMinutes(30), null, Duration.ofMinutes(1), "null"::equals);

        long duration = expiry.expireAfterCreate("key", "null", 0);

        assertThat(duration).isEqualTo(Duration.ofMinutes(1).toNanos());
    }
}
