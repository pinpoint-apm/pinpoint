package com.navercorp.pinpoint.common.trace;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class UriStatHistogramBucketTest {

    @Test
    public void getBucketSize() {
        Assert.assertEquals(UriStatHistogramBucket.values().length, UriStatHistogramBucket.getBucketSize());
    }

    @Test
    public void getValue() {
        Assert.assertEquals(UriStatHistogramBucket.UNDER_100, UriStatHistogramBucket.getValue(1));
    }
}