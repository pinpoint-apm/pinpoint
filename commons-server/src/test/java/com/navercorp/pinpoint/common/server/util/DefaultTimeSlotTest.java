package com.navercorp.pinpoint.common.server.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultTimeSlotTest {

    @Test
    void getTimeSlot() {
        TimeSlot timeSlot = new DefaultTimeSlot(1000);
        Assertions.assertEquals(1000, timeSlot.getTimeSlot(1001));
    }

    @Test
    void getTimeSlot_negative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new DefaultTimeSlot(-1));
    }
}