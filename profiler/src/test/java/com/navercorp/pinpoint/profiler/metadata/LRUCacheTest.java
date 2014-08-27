package com.nhn.pinpoint.profiler.metadata;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * @author emeroad
 */
public class LRUCacheTest {
    @Test
    public void testPut() throws Exception {
        long cacheSize = 100;
        LRUCache cache = new LRUCache((int) cacheSize);
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            cache.put(String.valueOf(random.nextInt(100000)));
        }

        long size = cache.getSize();
        Assert.assertEquals(size, cacheSize);

    }

    @Test
    public void testGetSize() throws Exception {
        LRUCache<String> cache = new LRUCache<String>(2);
        Assert.assertEquals(cache.getSize(), 0);

        String sqlObject = "test";

        boolean hit = cache.put(sqlObject);
        Assert.assertTrue(hit);
        Assert.assertEquals(cache.getSize(), 1);

        boolean hit2 = cache.put(sqlObject);
        Assert.assertFalse(hit2);
        Assert.assertEquals(cache.getSize(), 1);
//        "23 123";
//        "DCArMlhwQO 7"
        cache.put("23 123");
        cache.put("DCArMlhwQO 7");
        cache.put("3");
        cache.put("4");
        Assert.assertEquals(cache.getSize(), 2);


    }
}
