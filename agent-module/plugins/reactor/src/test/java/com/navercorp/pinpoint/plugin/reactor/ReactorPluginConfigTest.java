/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReactorPluginConfigTest {

    @Test
    void defaultConfig() {
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(new Properties());
        ReactorPluginConfig config = new ReactorPluginConfig(profilerConfig);

        assertTrue(config.isEnable());
        assertFalse(config.isTraceOnError());
        assertTrue(config.isTraceOnNext());
        assertTrue(config.isTracePublishOn());
        assertTrue(config.isTraceSubscribeOn());
        assertTrue(config.isTraceDelay());
        assertTrue(config.isTraceInterval());
        assertTrue(config.isTraceRetry());
        assertTrue(config.isTraceTimeout());
        assertTrue(config.isTraceSubscribe());
        assertFalse(config.isMarkErrorRetry());
        assertFalse(config.isMarkErrorOnError());
    }

    @Test
    void traceOnNextDisabled() {
        Properties properties = new Properties();
        properties.setProperty("profiler.reactor.trace.onNext", "false");
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);
        ReactorPluginConfig config = new ReactorPluginConfig(profilerConfig);

        assertFalse(config.isTraceOnNext());
    }

    @Test
    void traceOnNextEnabled() {
        Properties properties = new Properties();
        properties.setProperty("profiler.reactor.trace.onNext", "true");
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);
        ReactorPluginConfig config = new ReactorPluginConfig(profilerConfig);

        assertTrue(config.isTraceOnNext());
    }

    @Test
    void toStringContainsTraceOnNext() {
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(new Properties());
        ReactorPluginConfig config = new ReactorPluginConfig(profilerConfig);

        String str = config.toString();
        assertTrue(str.contains("traceOnNext="));
    }
}
