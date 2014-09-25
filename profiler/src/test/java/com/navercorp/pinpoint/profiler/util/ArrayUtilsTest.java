package com.nhn.pinpoint.profiler.util;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class ArrayUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(ArrayUtilsTest.class.getName());

    @Test
    public void dropToStringSmall() {
        byte[] bytes = new byte[] {1, 2, 3, 4};

        String small = ArrayUtils.dropToString(bytes, 3);
        Assert.assertEquals("[1, 2, 3, ...(1)]", small);
    }

    @Test
    public void dropToStringEqual() {
        byte[] bytes = new byte[] {1, 2, 3, 4};

        String equals = ArrayUtils.dropToString(bytes, 4);
        Assert.assertEquals("[1, 2, 3, 4]", equals);

    }

    @Test
    public void dropToStringLarge() {
        byte[] bytes = new byte[] {1, 2, 3, 4};

        String large = ArrayUtils.dropToString(bytes, 11);
        Assert.assertEquals("[1, 2, 3, 4]", large);

    }


    @Test
    public void dropToStringOneAndZero() {
        byte[] bytes = new byte[] {1, 2, 3, 4};

        String one = ArrayUtils.dropToString(bytes, 1);
        Assert.assertEquals("[1, ...(3)]", one);

        String zero = ArrayUtils.dropToString(bytes, 0);
        Assert.assertEquals("[...(4)]", zero);
    }


    @Test
    public void dropToStringSingle() {
        byte[] bytes = new byte[] {1};

        String small = ArrayUtils.dropToString(bytes, 1);
        logger.info(small);
        Assert.assertEquals("[1]", small);
    }

    @Test
    public void dropToStringNegative() {
        byte[] bytes = new byte[] {1};

        try {
            ArrayUtils.dropToString(bytes, -1);
            Assert.fail();
        } catch (Exception e) {
        }
    }
}
