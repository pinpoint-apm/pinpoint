package com.nhn.pinpoint.metadata;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 */
public class StringCacheTest {

    @Test
    public void put() {
        StringCache cache = new StringCache();
        Result test = cache.put("test");
        Assert.assertEquals(-1, test.getId());

        Result recheck = cache.put("test");
        Assert.assertEquals(test.getId(), recheck.getId());

        Result newValue = cache.put("new");
        Assert.assertEquals(1, newValue.getId());

    }
}
