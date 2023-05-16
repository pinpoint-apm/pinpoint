package com.navercorp.pinpoint.common.trace;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UriStatHistogramBucketTest {

    public static final UriStatHistogramBucket.Layout LAYOUT = UriStatHistogramBucket.getLayout();

    @Test
    public void getBucketSize() {
        Assertions.assertEquals(UriStatHistogramBucket.values().length, LAYOUT.getBucketSize());
    }

    @Test
    public void getValue() {
        Assertions.assertEquals(UriStatHistogramBucket.UNDER_100, LAYOUT.getBucket(1));
    }
}