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
import com.navercorp.pinpoint.profiler.sampler.SamplerType;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class UrlSamplerConfigTest {

    @Test
    public void emptyList() {
        Properties properties = new Properties();
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        UrlSamplerConfig urlSamplerConfig = new UrlSamplerConfig(profilerConfig, SamplerType.COUNTING);
        List<Map.Entry<Integer, UrlSamplerConfig.UrlInfo>> entryList = urlSamplerConfig.entryList();
        assertEquals(0, entryList.size());
    }

    @Test
    public void entryList() {
        Properties properties = new Properties();
        properties.setProperty("profiler.sampling.url.1.path", "/foo");
        properties.setProperty("profiler.sampling.url.1.counting.sampling-rate", "10");
        properties.setProperty("profiler.sampling.url.1.new.throughput", "0");
        properties.setProperty("profiler.sampling.url.1.continue.throughput", "0");
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        UrlSamplerConfig urlSamplerConfig = new UrlSamplerConfig(profilerConfig, SamplerType.COUNTING);
        List<Map.Entry<Integer, UrlSamplerConfig.UrlInfo>> entryList = urlSamplerConfig.entryList();
        UrlSamplerConfig.UrlInfo urlInfo = entryList.get(0).getValue();
        assertEquals("/foo", urlInfo.getUrlPath());
        assertNotNull(urlInfo.getSampler());
        assertEquals(0, urlInfo.getSamplingNewThroughput());
        assertEquals(0, urlInfo.getSamplingContinueThroughput());
        assertTrue(urlInfo.isValid());
    }

    @Test
    public void notFoundSampler() {
        Properties properties = new Properties();
        properties.setProperty("profiler.sampling.url.1.path", "/foo");
        properties.setProperty("profiler.sampling.url.1.percent.sampling-rate", "1");
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        UrlSamplerConfig urlSamplerConfig = new UrlSamplerConfig(profilerConfig, SamplerType.COUNTING);
        List<Map.Entry<Integer, UrlSamplerConfig.UrlInfo>> entryList = urlSamplerConfig.entryList();
        UrlSamplerConfig.UrlInfo urlInfo = entryList.get(0).getValue();
        assertNull(urlInfo.getSampler());
        assertFalse(urlInfo.isValid());
    }

    @Test
    public void invalidConfig() {
        Properties properties = new Properties();
        properties.setProperty("profiler.sampling.url.1x.path", "/foo");
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        UrlSamplerConfig urlSamplerConfig = new UrlSamplerConfig(profilerConfig, SamplerType.COUNTING);
        List<Map.Entry<Integer, UrlSamplerConfig.UrlInfo>> entryList = urlSamplerConfig.entryList();
        assertEquals(0, entryList.size());
    }
}