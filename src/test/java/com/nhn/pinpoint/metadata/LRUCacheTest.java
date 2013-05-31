package com.nhn.pinpoint.metadata;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Random;

/**
 *
 */
public class LRUCacheTest {
    @Test
    public void testPut() throws Exception {
        int cacheSize = 100;
        LRUCache LRUCache = new LRUCache(cacheSize);
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            LRUCache.put(new SqlObject(String.valueOf(random.nextInt(100000))));
        }

        int size = LRUCache.getSize();
        Assert.assertEquals(size, cacheSize);

    }

    @Test
    public void testGetSize() throws Exception {
        LRUCache LRUCache = new LRUCache(2);
        Assert.assertEquals(LRUCache.getSize(), 0);

        SqlObject sqlObject = new SqlObject("test");

        boolean hit = LRUCache.put(sqlObject);
        Assert.assertTrue(hit);
        Assert.assertEquals(LRUCache.getSize(), 1);

        boolean hit2 = LRUCache.put(sqlObject);
        Assert.assertFalse(hit2);
        Assert.assertEquals(LRUCache.getSize(), 1);
//        "23 123";
//        "DCArMlhwQO 7"
        LRUCache.put(new SqlObject("23 123"));
        LRUCache.put(new SqlObject("DCArMlhwQO 7"));
        LRUCache.put(new SqlObject("3"));
        LRUCache.put(new SqlObject("4"));
        Assert.assertEquals(LRUCache.getSize(), 2);


    }
}
