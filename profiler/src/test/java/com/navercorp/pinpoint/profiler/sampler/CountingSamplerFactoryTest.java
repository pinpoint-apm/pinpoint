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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;

import org.junit.Assert;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author emeroad
 */
public class CountingSamplerFactoryTest {
    @Test
    public void createSamplerSamplingRate0() {
        SamplerFactory factory = new CountingSamplerFactory(0);
        Sampler sampler = factory.createSampler();
        boolean sampling = sampler.isSampling();
        Assert.assertFalse(sampling);
    }

    @Test
    public void createSamplerSamplingRate_Negative() {
        SamplerFactory factory = new CountingSamplerFactory(-1);
        Sampler sampler = factory.createSampler();
        boolean sampling = sampler.isSampling();
        Assert.assertFalse(sampling);
    }

    @Test
    public void sampling() {
        SamplerFactory factory = new CountingSamplerFactory(2);
        Sampler sampler = factory.createSampler();

        Assert.assertEquals(true, sampler.isSampling());
        Assert.assertEquals(false, sampler.isSampling());
        Assert.assertEquals(true, sampler.isSampling());
        Assert.assertEquals(false, sampler.isSampling());
    }

    @Test
    public void legacy_samplingRate() {
        ProfilerConfig profilerConfig = mock(ProfilerConfig.class);
        when(profilerConfig.readInt(CountingSamplerFactory.LEGACY_SAMPLING_RATE_NAME, -1)).thenReturn(100);

        CountingSamplerFactory.Config config = CountingSamplerFactory.config(profilerConfig);
        Assert.assertEquals(100, config.getSamplingRate());
    }

    @Test
    public void new_samplingRate() {
        ProfilerConfig profilerConfig = mock(ProfilerConfig.class);
        when(profilerConfig.readInt(CountingSamplerFactory.LEGACY_SAMPLING_RATE_NAME, -1)).thenReturn(-1);
        when(profilerConfig.readInt(CountingSamplerFactory.SAMPLING_RATE_NAME, 1)).thenReturn(200);

        CountingSamplerFactory.Config config = CountingSamplerFactory.config(profilerConfig);
        Assert.assertEquals(200, config.getSamplingRate());
    }
}
