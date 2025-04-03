package com.navercorp.pinpoint.common.timeseries.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MathUtilsTest {
    @Test
    public void roundToNearestMultipleOf() {
        Assertions.assertEquals(1, MathUtils.roundToNearestMultipleOf(1, 1));
        Assertions.assertEquals(4, MathUtils.roundToNearestMultipleOf(1, 4));
        Assertions.assertEquals(4, MathUtils.roundToNearestMultipleOf(2, 4));
        Assertions.assertEquals(4, MathUtils.roundToNearestMultipleOf(3, 4));
        Assertions.assertEquals(4, MathUtils.roundToNearestMultipleOf(4, 4));
        Assertions.assertEquals(4, MathUtils.roundToNearestMultipleOf(5, 4));
        Assertions.assertEquals(8, MathUtils.roundToNearestMultipleOf(6, 4));
        Assertions.assertEquals(8, MathUtils.roundToNearestMultipleOf(7, 4));
        Assertions.assertEquals(8, MathUtils.roundToNearestMultipleOf(8, 4));
        Assertions.assertEquals(10, MathUtils.roundToNearestMultipleOf(10, 5));
        Assertions.assertEquals(10, MathUtils.roundToNearestMultipleOf(11, 5));
        Assertions.assertEquals(10, MathUtils.roundToNearestMultipleOf(12, 5));
        Assertions.assertEquals(15, MathUtils.roundToNearestMultipleOf(13, 5));
        Assertions.assertEquals(15, MathUtils.roundToNearestMultipleOf(14, 5));
        Assertions.assertEquals(15, MathUtils.roundToNearestMultipleOf(15, 5));
        Assertions.assertEquals(15, MathUtils.roundToNearestMultipleOf(16, 5));
        Assertions.assertEquals(15, MathUtils.roundToNearestMultipleOf(17, 5));
        Assertions.assertEquals(20, MathUtils.roundToNearestMultipleOf(18, 5));
        Assertions.assertEquals(20, MathUtils.roundToNearestMultipleOf(19, 5));
        Assertions.assertEquals(20, MathUtils.roundToNearestMultipleOf(20, 5));
        Assertions.assertEquals(5000, MathUtils.roundToNearestMultipleOf(6000, 5000));
        Assertions.assertEquals(10000, MathUtils.roundToNearestMultipleOf(9000, 5000));
    }
}