/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ktor;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KtorPluginConfigTest {

    @Test
    void clientDisabledByDefault() {
        ProfilerConfig config = new DefaultProfilerConfig();
        KtorPluginConfig pluginConfig = new KtorPluginConfig(config);

        assertFalse(pluginConfig.isClientEnable());
        assertTrue(pluginConfig.isClientParam());
        assertTrue(pluginConfig.isClientMarkError());
    }

    @Test
    void clientEnabledFlagReadsTrue() {
        DefaultProfilerConfig config = new DefaultProfilerConfig();
        config.getProperties().setProperty("profiler.ktor.client.enable", "true");

        KtorPluginConfig pluginConfig = new KtorPluginConfig(config);

        assertTrue(pluginConfig.isClientEnable());
        assertTrue(pluginConfig.isClientParam());
        assertTrue(pluginConfig.isClientMarkError());
    }

    @Test
    void paramAndMarkErrorRespectFlags() {
        DefaultProfilerConfig config = new DefaultProfilerConfig();
        config.getProperties().setProperty("profiler.ktor.client.enable", "true");
        config.getProperties().setProperty("profiler.ktor.client.param", "false");
        config.getProperties().setProperty("profiler.ktor.client.mark.error", "false");

        KtorPluginConfig pluginConfig = new KtorPluginConfig(config);

        assertTrue(pluginConfig.isClientEnable());
        assertFalse(pluginConfig.isClientParam());
        assertFalse(pluginConfig.isClientMarkError());
    }
}
