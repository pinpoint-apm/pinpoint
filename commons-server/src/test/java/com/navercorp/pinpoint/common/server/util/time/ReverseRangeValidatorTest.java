package com.navercorp.pinpoint.common.server.util.time;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

class ReverseRangeValidatorTest {

    @Test
    public void check() {
        RangeValidator rangeValidator = new ReverseRangeValidator(Duration.ofDays(2));

        rangeValidator.validate(ofDays(2), Instant.EPOCH);

        rangeValidator.validate(Range.unchecked(ofDays(2).toEpochMilli(), Instant.EPOCH.toEpochMilli()));
    }

    @Test
    public void check_failure() {
        RangeValidator rangeValidator = new ReverseRangeValidator(Duration.ofDays(2));

        Assertions.assertThrows(Exception.class, () -> {
            rangeValidator.validate(ofDays(3), Instant.EPOCH);
        });
    }

    private Instant ofDays(long days) {
        return Instant.EPOCH.plus(Duration.ofDays(days));
    }

}