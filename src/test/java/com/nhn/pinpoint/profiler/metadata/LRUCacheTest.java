package com.nhn.pinpoint.profiler.metadata;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Random;

/**
 *
 */
public class LRUCacheTest {
    @Test
    public void testPut() throws Exception {
        long cacheSize = 100;
        LRUCache LRUCache = new LRUCache((int) cacheSize);
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            LRUCache.put(new SqlObject(String.valueOf(random.nextInt(100000))));
        }

        long size = LRUCache.getSize();
        Assert.assertEquals(size, cacheSize);

    }

    @Test
    public void testGetSize() throws Exception {
        LRUCache cache = new LRUCache(2);
        Assert.assertEquals(cache.getSize(), 0);

        SqlObject sqlObject = new SqlObject("test");

        boolean hit = cache.put(sqlObject);
        Assert.assertTrue(hit);
        Assert.assertEquals(cache.getSize(), 1);

        boolean hit2 = cache.put(sqlObject);
        Assert.assertFalse(hit2);
        Assert.assertEquals(cache.getSize(), 1);
//        "23 123";
//        "DCArMlhwQO 7"
        cache.put(new SqlObject("23 123"));
        cache.put(new SqlObject("DCArMlhwQO 7"));
        cache.put(new SqlObject("3"));
        cache.put(new SqlObject("4"));
        Assert.assertEquals(cache.getSize(), 2);


    }
}
