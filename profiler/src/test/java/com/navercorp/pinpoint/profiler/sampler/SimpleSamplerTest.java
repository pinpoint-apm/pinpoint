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

package com.navercorp.pinpoint.profiler.sampler;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class SimpleSamplerTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void test() {
        CountingSampler simpleSampler = new CountingSampler(1);
        assertChoice(simpleSampler);
        assertChoice(simpleSampler);
        assertChoice(simpleSampler);
        assertChoice(simpleSampler);

         CountingSampler simpleSampler2 = new CountingSampler(2);
        assertChoice(simpleSampler2);
        assertDrop(simpleSampler2);
        assertChoice(simpleSampler2);
        assertDrop(simpleSampler2);
    }

    @Test
    public void mod() {
        int i = 0 % 101;
        logger.debug("{}", i);

        int j = Math.abs(-102) % 101;
        logger.debug("{}", j);
    }

    private void assertDrop(CountingSampler simpleSampler) {
        boolean sample = simpleSampler.isSampling();
        Assert.assertFalse(sample);
    }

    private void assertChoice(CountingSampler simpleSampler) {
        boolean sample = simpleSampler.isSampling();
        Assert.assertTrue(sample);
    }
}
