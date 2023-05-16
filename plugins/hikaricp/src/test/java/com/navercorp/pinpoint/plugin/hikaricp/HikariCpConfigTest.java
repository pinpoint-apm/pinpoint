/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.hikaricp;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class HikariCpConfigTest {

    @Test
    public void configTest1() throws Exception {
        HikariCpConfig hikariCpConfig = createHikariCpConfig("false", "false");

        Assertions.assertFalse(hikariCpConfig.isPluginEnable());
        Assertions.assertFalse(hikariCpConfig.isProfileClose());
    }

    @Test
    public void configTest2() throws Exception {
        HikariCpConfig hikariCpConfig = createHikariCpConfig("false", "true");

        Assertions.assertFalse(hikariCpConfig.isPluginEnable());
        Assertions.assertTrue(hikariCpConfig.isProfileClose());
    }

    @Test
    public void configTest3() throws Exception {
        HikariCpConfig hikariCpConfig = createHikariCpConfig("true", "false");

        Assertions.assertTrue(hikariCpConfig.isPluginEnable());
        Assertions.assertFalse(hikariCpConfig.isProfileClose());
    }

    @Test
    public void configTest4() throws Exception {
        HikariCpConfig hikariCpConfig = createHikariCpConfig("true", "true");

        Assertions.assertTrue(hikariCpConfig.isPluginEnable());
        Assertions.assertTrue(hikariCpConfig.isProfileClose());
    }

    private HikariCpConfig createHikariCpConfig(String pluginEnable, String profileConnectionCloseEnable) {
        Properties properties = new Properties();
        properties.put(HikariCpConfig.HIKARICP_PLUGIN_ENABLE, pluginEnable);
        properties.put(HikariCpConfig.HIKARICP_PROFILE_CONNECTIONCLOSE_ENABLE, profileConnectionCloseEnable);

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        return new HikariCpConfig(profilerConfig);
    }
}
