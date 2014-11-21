package com.nhn.pinpoint.common.util;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class MathUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Test
    public void fastAbs() {
        Assert.assertTrue(MathUtils.fastAbs(-1) > 0);
        Assert.assertTrue(MathUtils.fastAbs(0) == 0);
        Assert.assertTrue(MathUtils.fastAbs(1) > 0);
    }

    @Test
    public void overflow() {

        logger.debug("abs:{}", Math.abs(Integer.MIN_VALUE));
        logger.debug("fastabs:{}", MathUtils.fastAbs(Integer.MIN_VALUE));

        int index = Integer.MIN_VALUE - 2;
        for (int i = 0; i < 5; i++) {
            logger.debug("{}------------", i);
            logger.debug("{}", index);
            logger.debug("mod:{}", index % 3);
            logger.debug("abs:{}", Math.abs(index));
            logger.debug("fastabs:{}", MathUtils.fastAbs(index));

            index++;
        }
    }
    
    @Test
    public void roundToNearestMultipleOf() {
        Assert.assertEquals(1, MathUtils.roundToNearestMultipleOf(1, 1));
        Assert.assertEquals(4, MathUtils.roundToNearestMultipleOf(1, 4));
        Assert.assertEquals(4, MathUtils.roundToNearestMultipleOf(2, 4));
        Assert.assertEquals(4, MathUtils.roundToNearestMultipleOf(3, 4));
        Assert.assertEquals(4, MathUtils.roundToNearestMultipleOf(4, 4));
        Assert.assertEquals(4, MathUtils.roundToNearestMultipleOf(5, 4));
        Assert.assertEquals(8, MathUtils.roundToNearestMultipleOf(6, 4));
        Assert.assertEquals(8, MathUtils.roundToNearestMultipleOf(7, 4));
        Assert.assertEquals(8, MathUtils.roundToNearestMultipleOf(8, 4));
        Assert.assertEquals(10, MathUtils.roundToNearestMultipleOf(10, 5));
        Assert.assertEquals(10, MathUtils.roundToNearestMultipleOf(11, 5));
        Assert.assertEquals(10, MathUtils.roundToNearestMultipleOf(12, 5));
        Assert.assertEquals(15, MathUtils.roundToNearestMultipleOf(13, 5));
        Assert.assertEquals(15, MathUtils.roundToNearestMultipleOf(14, 5));
        Assert.assertEquals(15, MathUtils.roundToNearestMultipleOf(15, 5));
        Assert.assertEquals(15, MathUtils.roundToNearestMultipleOf(16, 5));
        Assert.assertEquals(15, MathUtils.roundToNearestMultipleOf(17, 5));
        Assert.assertEquals(20, MathUtils.roundToNearestMultipleOf(18, 5));
        Assert.assertEquals(20, MathUtils.roundToNearestMultipleOf(19, 5));
        Assert.assertEquals(20, MathUtils.roundToNearestMultipleOf(20, 5));
        Assert.assertEquals(5000, MathUtils.roundToNearestMultipleOf(6000, 5000));
        Assert.assertEquals(10000, MathUtils.roundToNearestMultipleOf(9000, 5000));
    }

}
