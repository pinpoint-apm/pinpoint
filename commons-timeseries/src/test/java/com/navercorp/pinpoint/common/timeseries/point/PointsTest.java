package com.navercorp.pinpoint.common.timeseries.point;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class PointsTest {

    @Test
    void ofLong() {
        DataPoint<Long> point = Points.ofLong(100, 200);
        Assertions.assertEquals(100, point.getTimestamp());
        Assertions.assertEquals(200, point.getValue());
    }

    @Test
    void ofDouble() {
        DataPoint<Double> point = Points.ofDouble(100, 200);
        Assertions.assertEquals(100, point.getTimestamp());
        Assertions.assertEquals(200, point.getValue());
    }
}