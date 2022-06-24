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

package com.navercorp.pinpoint.profiler.cache;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.hamcrest.Matchers.is;

/**
 * @author emeroad
 */
public class LRUCacheTest {
    @Test
    public void testPut() {
        long cacheSize = 100;
        LRUCache<String> cache = new LRUCache<>((int) cacheSize);
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            cache.put(String.valueOf(random.nextInt(100000)));
        }
        ConditionFactory await = Awaitility.await();
        await.until(cache::getSize, is(cacheSize));
    }

    @Test
    public void testGetSize() {
        LRUCache<String> cache = new LRUCache<>(2);
        Assertions.assertEquals(cache.getSize(), 0);

        String sqlObject = "test";

        boolean hit = cache.put(sqlObject);
        Assertions.assertTrue(hit);
        Assertions.assertEquals(cache.getSize(), 1);

        boolean hit2 = cache.put(sqlObject);
        Assertions.assertFalse(hit2);
        ConditionFactory await = Awaitility.await();
        await.until(cache::getSize, is(1L));
//        "23 123";
//        "DCArMlhwQO 7"
        cache.put("23 123");
        cache.put("DCArMlhwQO 7");
        cache.put("3");
        cache.put("4");

        await.until(cache::getSize, is(2L));

    }
}
