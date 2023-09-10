package com.navercorp.pinpoint.common.server.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class TimstampUtilsTest {

    @Test
    public void testReverseRoundedTimeMillis1() {
        long from = new Random().nextLong();
        long to = from + TimestampUtils.getTimeslotSize();

        long reverseRoundedFrom = TimestampUtils.reverseRoundedTimeMillis(from);
        long reverseRoundedTo = TimestampUtils.reverseRoundedTimeMillis(to);

        Assertions.assertTrue(reverseRoundedFrom >= reverseRoundedTo);
    }

    @Test
    public void testReverseRoundedTimeMillis2() {
        long from = new Random().nextLong();
        long to = from + 1;

        long reverseRoundedFrom = TimestampUtils.reverseRoundedTimeMillis(from);
        long reverseRoundedTo = TimestampUtils.reverseRoundedTimeMillis(to);

        Assertions.assertTrue(reverseRoundedFrom >= reverseRoundedTo);
    }
}
