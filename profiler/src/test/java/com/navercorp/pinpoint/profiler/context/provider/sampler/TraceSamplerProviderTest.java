/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.sampler;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import com.navercorp.pinpoint.bootstrap.sampler.TraceSampler;
import com.navercorp.pinpoint.profiler.context.config.DefaultContextConfig;
import com.navercorp.pinpoint.profiler.context.id.AtomicIdGenerator;
import com.navercorp.pinpoint.profiler.sampler.BasicTraceSampler;
import com.navercorp.pinpoint.profiler.sampler.RateLimitTraceSampler;
import com.navercorp.pinpoint.profiler.sampler.TrueSampler;
import com.navercorp.pinpoint.profiler.sampler.UrlTraceSampler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class TraceSamplerProviderTest {

    @Test
    public void get() {
        Properties properties = new Properties();
        properties.setProperty("profiler.sampling.url.1.path", "/foo");
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        TraceSamplerProvider provider = new TraceSamplerProvider(profilerConfig, new DefaultContextConfig(), TrueSampler.INSTANCE, new AtomicIdGenerator());
        TraceSampler traceSampler = provider.get();
        assertNotNull(traceSampler);
    }

    @Test
    public void getUrlSampler() {
        Properties properties = new Properties();
        properties.setProperty("profiler.sampling.enable", "true");
        properties.setProperty("profiler.sampling.url.enable", "true");
        properties.setProperty("profiler.sampling.url.1.path", "/foo");

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        TraceSamplerProvider provider = new TraceSamplerProvider(profilerConfig, new DefaultContextConfig(), TrueSampler.INSTANCE, new AtomicIdGenerator());
        TraceSampler traceSampler = provider.get();
        if(Boolean.FALSE == (traceSampler instanceof UrlTraceSampler)) {
            Assert.fail("Unexpected sampler type. traceSampler=" + traceSampler);
        }
    }

    @Test
    public void samplingEnableFalse() {
        Properties properties = new Properties();
        properties.setProperty("profiler.sampling.enable", "false");
        properties.setProperty("profiler.sampling.url.enable", "true");
        properties.setProperty("profiler.sampling.url.1.path", "/foo");

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        TraceSamplerProvider provider = new TraceSamplerProvider(profilerConfig, new DefaultContextConfig(), TrueSampler.INSTANCE, new AtomicIdGenerator());
        TraceSampler traceSampler = provider.get();
        if(Boolean.FALSE == (traceSampler instanceof BasicTraceSampler)) {
            Assert.fail("Unexpected sampler type. traceSampler=" + traceSampler);
        }
    }

    @Test
    public void urlSamplingEnableFalse() {
        Properties properties = new Properties();
        properties.setProperty("profiler.sampling.enable", "true");
        properties.setProperty("profiler.sampling.url.enable", "false");
        properties.setProperty("profiler.sampling.url.1.path", "/foo");

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        TraceSamplerProvider provider = new TraceSamplerProvider(profilerConfig, new DefaultContextConfig(), TrueSampler.INSTANCE, new AtomicIdGenerator());
        TraceSampler traceSampler = provider.get();
        if(Boolean.FALSE == (traceSampler instanceof BasicTraceSampler)) {
            Assert.fail("Unexpected sampler type. traceSampler=" + traceSampler);
        }
    }
}