package com.nhn.pinpoint.common.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class TimeUtilsTest {
    @Test
    public void testReverseCurrentTimeMillis() throws Exception {
        long currentTime = System.currentTimeMillis();
        long reverseTime = TimeUtils.reverseTimeMillis(currentTime);
        long recoveryTime = TimeUtils.recoveryTimeMillis(reverseTime);

        Assert.assertEquals(currentTime, recoveryTime);
    }

    @Test
    public void testTimeOrder() throws InterruptedException {
        long l1 = TimeUtils.reverseCurrentTimeMillis();
        Thread.sleep(5);
        long l2 = TimeUtils.reverseCurrentTimeMillis();

        Assert.assertTrue(l1 > l2);
    }

}
