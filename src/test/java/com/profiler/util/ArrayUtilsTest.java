package com.profiler.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.logging.Logger;

public class ArrayUtilsTest {
    private final Logger logger = Logger.getLogger(ArrayUtilsTest.class.getName());
    @Test
    public void toStringTest() {
        byte[] bytes = new byte[] {1, 2, 3, 4};

        String small = ArrayUtils.dropToString(bytes, 3);
        logger.info(small);
        Assert.assertEquals("[1, 2, 3, ...(4)]", small);

        String large = ArrayUtils.dropToString(bytes, 11);
        logger.info(large);
        Assert.assertEquals("[1, 2, 3, 4]", large);

        String one = ArrayUtils.dropToString(bytes, 1);
        logger.info(one);
        Assert.assertEquals("[1, ...(4)]", one);

        String zero = ArrayUtils.dropToString(bytes, 0);
        logger.info(zero);
        Assert.assertEquals("[...(4)]", zero);
    }
}
