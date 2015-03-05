/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.metadata;

import org.junit.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.profiler.metadata.LRUCache;

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
