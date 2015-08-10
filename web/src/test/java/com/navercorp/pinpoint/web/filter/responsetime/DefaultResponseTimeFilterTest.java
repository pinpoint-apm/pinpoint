package com.navercorp.pinpoint.web.filter.responsetime;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author emeroad
 */
public class DefaultResponseTimeFilterTest {

    @Test
    public void testAccept() throws Exception {

        ResponseTimeFilter filter1 = new DefaultResponseTimeFilter(1000, 2000);

        Assert.assertTrue(filter1.accept(1100));

        // between
        Assert.assertTrue(filter1.accept(1000));
        Assert.assertTrue(filter1.accept(2000));

        // lower
        Assert.assertFalse(filter1.accept(500));
        // upper
        Assert.assertFalse(filter1.accept(2500));

    }
}