package com.nhn.pinpoint.common.util;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

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

        int index = Integer.MIN_VALUE -2;
        for(int i =0; i<5; i++) {
            logger.debug("{}------------", i);
            logger.debug("{}", index);
            logger.debug("mod:{}", index % 3);
            logger.debug("abs:{}", Math.abs(index));
            logger.debug("fastabs:{}", MathUtils.fastAbs(index));

            index++;
        }



    }


}
