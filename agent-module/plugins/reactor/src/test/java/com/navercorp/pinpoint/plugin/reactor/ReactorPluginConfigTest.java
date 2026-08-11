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

package com.navercorp.pinpoint.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class ReactorPluginConfigTest {

    @Test
    public void periodicSchedulerTask_defaultsToFalse() {
        ReactorPluginConfig config = createConfig(new Properties());

        Assertions.assertFalse(config.isTracePeriodicSchedulerTask());
    }

    @Test
    public void periodicSchedulerTask_isIndependentFromOneShotCarrier() {
        Properties properties = new Properties();
        properties.put("profiler.reactor.trace.scheduler.task.periodic", "true");

        ReactorPluginConfig config = createConfig(properties);

        Assertions.assertTrue(config.isTracePeriodicSchedulerTask());
        Assertions.assertFalse(config.isTraceSchedulerTask());
    }

    private ReactorPluginConfig createConfig(Properties properties) {
        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);
        return new ReactorPluginConfig(profilerConfig);
    }
}
