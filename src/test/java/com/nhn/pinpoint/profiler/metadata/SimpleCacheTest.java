package com.nhn.pinpoint.profiler.metadata;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 */
public class SimpleCacheTest {

    @Test
    public void put() {
        SimpleCache<String> cache = new SimpleCache<String>();
        Result test = cache.put("test");
        Assert.assertEquals(-1, test.getId());
        Assert.assertTrue(test.isNewValue());

        Result recheck = cache.put("test");
        Assert.assertEquals(test.getId(), recheck.getId());
        Assert.assertFalse(recheck.isNewValue());

        Result newValue = cache.put("new");
        Assert.assertEquals(1, newValue.getId());
        Assert.assertTrue(newValue.isNewValue());

    }
}
