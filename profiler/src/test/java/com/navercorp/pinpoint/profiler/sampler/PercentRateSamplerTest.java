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

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class PercentRateSamplerTest {

    @Test
    public void test_75p() {
        Sampler sampler = new PercentRateSampler(75_00);
        assertChoice(sampler);
        assertChoice(sampler);
        assertChoice(sampler);
        assertDrop(sampler);
    }

    @Test
    public void test_50p() {
        Sampler sampler = new PercentRateSampler(50_00);
        assertChoice(sampler);
        assertDrop(sampler);
        assertChoice(sampler);
        assertDrop(sampler);
    }

    @Test
    public void test_25p() {
        Sampler sampler = new PercentRateSampler(25_00);
        assertChoice(sampler);
        assertDrop(sampler);
        assertDrop(sampler);
        assertDrop(sampler);
    }

    @Test
    public void test_33p() {
        // 30% = 30.03030303....
        Sampler sampler = new PercentRateSampler(33_00);
        for (int i = 0; i < 33; i++) {
            assertChoice(sampler);
            assertDrop(sampler);
            assertDrop(sampler);
        }
        assertDrop(sampler);
        assertChoice(sampler);
        assertDrop(sampler);
    }

    @Test
    public void test_10p() {
        Sampler sampler = new PercentRateSampler(10_00);
        assertChoice(sampler);
        for (int i = 0; i < 9; i++) {
            assertDrop(sampler);
        }
        assertChoice(sampler);
    }

    @Test
    public void test_1p() {
        Sampler sampler = new PercentRateSampler(1_00);
        assertChoice(sampler);
        for (int i = 0; i < 99; i++) {
            assertDrop(sampler);
        }
        assertChoice(sampler);
    }


    private void assertDrop(Sampler simpleSampler) {
        boolean sample = simpleSampler.isSampling();
        Assertions.assertFalse(sample);
    }

    private void assertChoice(Sampler simpleSampler) {
        boolean sample = simpleSampler.isSampling();
        Assertions.assertTrue(sample);
    }
}
