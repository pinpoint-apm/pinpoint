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
}
