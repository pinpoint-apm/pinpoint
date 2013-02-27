package com.profiler.common.util;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TimeSlotTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testGetStatisticsRowSlot() throws Exception {
        long currentTime = System.currentTimeMillis();
        // 슬롯 넘버를 알아온다.
        long statisticsRowSlot = TimeSlot.getStatisticsRowSlot(currentTime);

        logger.info("{} currentTime ", currentTime);
        logger.info("{} statisticsRowSlot", statisticsRowSlot);
        Assert.assertTrue(currentTime> statisticsRowSlot);

     }
}
