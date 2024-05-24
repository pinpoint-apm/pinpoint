package com.navercorp.pinpoint.common.hbase.async;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConcurrencyLimiterHelperTest {

    @Test
    void acquire() {

        ConcurrencyLimiterHelper limiter = new ConcurrencyLimiterHelper(1);
        limiter.acquire(1);
        Assertions.assertFalse(limiter.acquire(1));

        limiter.release(1);

        Assertions.assertEquals(0, limiter.count());
    }
}