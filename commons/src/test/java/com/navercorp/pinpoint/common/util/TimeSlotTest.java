package com.nhn.pinpoint.common.util;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class TimeSlotTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TimeSlot timeSlot = new DefaultTimeSlot();

    @Test
    public void testGetTimeSlot() throws Exception {
        long currentTime = System.currentTimeMillis();
        // 슬롯 넘버를 알아온다.
        long timeSlot = this.timeSlot.getTimeSlot(currentTime);

        logger.info("{} currentTime ", currentTime);
        logger.info("{} timeSlot", timeSlot);
        Assert.assertTrue(currentTime >= timeSlot);
     }

    @Test
    public void testSlotTime1() throws Exception {
        int slotTest = 60 * 1000;

        // 슬롯 넘버를 알아온다.
        long timeSlot = this.timeSlot.getTimeSlot(slotTest);

        logger.info("{} slotTest ", slotTest);
        logger.info("{} timeSlot", timeSlot);
        Assert.assertEquals(slotTest, timeSlot);
    }

    @Test
    public void testSlotTime2() throws Exception {
        int sourceTest = 60 * 1000;
        int slotTest = sourceTest + 1;


        // 슬롯 넘버를 알아온다.
        long timeSlot = this.timeSlot.getTimeSlot(slotTest);

        logger.info("{} slotTest ", slotTest);
        logger.info("{} timeSlot", timeSlot);
        Assert.assertEquals(sourceTest, timeSlot);
    }
}
