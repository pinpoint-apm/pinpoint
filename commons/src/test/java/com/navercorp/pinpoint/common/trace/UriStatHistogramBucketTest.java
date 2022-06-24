package com.navercorp.pinpoint.common.trace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UriStatHistogramBucketTest {

    @Test
    public void getBucketSize() {
        Assertions.assertEquals(UriStatHistogramBucket.values().length, UriStatHistogramBucket.getBucketSize());
    }

    @Test
    public void getValue() {
        Assertions.assertEquals(UriStatHistogramBucket.UNDER_100, UriStatHistogramBucket.getValue(1));
    }
}