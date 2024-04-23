package com.navercorp.pinpoint.common.server.util.time;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

class ForwardRangeValidatorTest {

    @Test
    public void check() {
        RangeValidator rangeValidator = new ForwardRangeValidator(Duration.ofDays(2));

        rangeValidator.validate(Instant.EPOCH, ofDays(2));

        Instant time = Instant.ofEpochMilli(1000);
        rangeValidator.validate(time, time.plus(Duration.ofDays(2)));

        rangeValidator.validate(ofDays(2), ofDays(2));
    }

    @Test
    public void checkRange() {
        RangeValidator rangeValidator = new ForwardRangeValidator(Duration.ofDays(2));

        rangeValidator.validate(Range.between(Instant.EPOCH, ofDays(2)));

        Instant time = Instant.ofEpochMilli(1000);
        rangeValidator.validate(Range.between(time, time.plus(Duration.ofDays(2))));

        rangeValidator.validate(Range.between(ofDays(2), ofDays(2)));
    }

    @Test
    public void checkFail() {
        RangeValidator rangeValidator = new ForwardRangeValidator(Duration.ofDays(2));
        Assertions.assertThrows(Exception.class, () -> {
            rangeValidator.validate(Instant.EPOCH, ofDays(2).plusMillis(1));
        });

        Assertions.assertThrows(Exception.class, () -> {
            rangeValidator.validate(ofDays(2), Instant.EPOCH);
        });
    }

    @Test
    public void checkRangeFail() {
        RangeValidator rangeValidator = new ForwardRangeValidator(Duration.ofDays(2));

        Assertions.assertThrows(Exception.class, () -> {
            rangeValidator.validate(Range.between(Instant.EPOCH, ofDays(2).plusMillis(1)));
        });

        Assertions.assertThrows(Exception.class, () -> {
            rangeValidator.validate(Range.between(ofDays(2), Instant.EPOCH));
        });
    }

    private Instant ofDays(long days) {
        return Instant.EPOCH.plus(Duration.ofDays(days));
    }

    @Test
    public void checkRange_Nanos() {
        RangeValidator rangeValidator = new ForwardRangeValidator(Duration.ofNanos(1));

        Assertions.assertThrows(Exception.class, () -> {
            rangeValidator.validate(Instant.EPOCH, Instant.EPOCH.plusNanos(2L));
        });

    }

}