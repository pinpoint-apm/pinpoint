/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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


/**
 * @author emeroad
 */
public class SimpleCacheTest {

    private SimpleCache.IdTransformer idTransformer = new SimpleCache.ZigZagTransformer();

    @Test
    public void startKey0() {
        SimpleCache<String> cache = new SimpleCache<>(idTransformer, 1024, 0);
        Result test = cache.put("test");
        Assert.assertEquals(0, test.getId());
    }

    @Test
    public void startKey1() {
        SimpleCache<String> cache = new SimpleCache<>(idTransformer, 1);
        Result test = cache.put("test");
        Assert.assertEquals(-1, test.getId());
    }

    @Test
    public void put() {
        SimpleCache<String> cache = new SimpleCache<>(idTransformer);
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
