package com.nhn.pinpoint.web.vo;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author emeroad
 */
public class RangeTest {
    @Test
    public void testCreate() {
        Range range1 = new Range(0, 0);
        Range range2 = new Range(0, 1);

        try {
            Range range3 = new Range(0, -1);
            Assert.fail();
        } catch (Exception e) {
        }

    }

    @Test
    public void testRange() {
        Range range1 =  new Range(0, 0);
        Assert.assertEquals(range1.getRange(), 0);

        Range range2 =  new Range(0, 1);
        Assert.assertEquals(range2.getRange(), 1);
    }
}
