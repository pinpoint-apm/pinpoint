package com.navercorp.pinpoint.common.server.util.timewindow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

class TimePrecisionTest {

    @Test
    void newTimePrecision() {
        TimePrecision precision = new TimePrecision(TimeUnit.MILLISECONDS, 1000);
        Assertions.assertEquals(1000, precision.getInterval());
    }

    @Test
    void newTimePrecision_negative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TimePrecision(TimeUnit.MILLISECONDS, -1));
    }
}