package com.nhn.pinpoint.web.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class LimitUtilsTest {
    @Test
    public void testCheckLimit() throws Exception {
        int equals = LimitUtils.checkRange(LimitUtils.MAX);
        Assert.assertEquals(equals, LimitUtils.MAX);

        int over = LimitUtils.checkRange(LimitUtils.MAX + 1);
        Assert.assertEquals(over, LimitUtils.MAX);

        int low = LimitUtils.checkRange(0);
        Assert.assertEquals(low, 0);

        try {
            LimitUtils.checkRange(-1);
            Assert.fail();
        } catch (Exception e) {
        }

    }
}
