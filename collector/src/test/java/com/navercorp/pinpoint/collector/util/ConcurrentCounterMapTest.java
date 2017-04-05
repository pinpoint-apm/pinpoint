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

package com.navercorp.pinpoint.collector.util;

import org.junit.Assert;

import org.junit.Test;

import java.util.Map;

/**
 * @author emeroad
 */
@Deprecated
public class ConcurrentCounterMapTest {
    @Test
    public void testIncrement() throws Exception {
        ConcurrentCounterMap<String> cache = new ConcurrentCounterMap<>();
        cache.increment("a", 1L);
        cache.increment("a", 2L);
        cache.increment("b", 5L);


        Map<String,ConcurrentCounterMap.LongAdder> remove = cache.remove();
        Assert.assertEquals(remove.get("a").get(), 3L);
        Assert.assertEquals(remove.get("b").get(), 5L);

        cache.increment("a", 1L);
        Map<String, ConcurrentCounterMap.LongAdder> remove2 = cache.remove();
        Assert.assertEquals(remove2.get("a").get(), 1L);
    }

    @Test
    public void testIntegerMax() throws Exception {
        ConcurrentCounterMap<String> cache = new ConcurrentCounterMap<>(16, Integer.MAX_VALUE);
        cache.increment("a", 1L);
        cache.increment("a", 2L);
        cache.increment("b", 5L);

    }

    @Test
    public void testIntegerMin() throws Exception {
        ConcurrentCounterMap<String> cache = new ConcurrentCounterMap<>(16, Integer.MIN_VALUE);
        cache.increment("a", 1L);
        cache.increment("a", 2L);
        cache.increment("b", 5L);

    }

}
