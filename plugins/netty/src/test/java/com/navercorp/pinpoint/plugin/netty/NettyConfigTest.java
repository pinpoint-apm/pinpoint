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

package com.navercorp.pinpoint.plugin.netty;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class NettyConfigTest {

    @Test
    public void configTest1() throws Exception {
        NettyConfig config = createConfig("true", "true");

        Assertions.assertTrue(config.isPluginEnable());
        Assertions.assertTrue(config.isHttpCodecEnable());
    }

    @Test
    public void configTest2() throws Exception {
        NettyConfig config = createConfig("true", "false");

        Assertions.assertTrue(config.isPluginEnable());
        Assertions.assertFalse(config.isHttpCodecEnable());
    }

    @Test
    public void configTest3() throws Exception {
        NettyConfig config = createConfig("false", "true");

        Assertions.assertFalse(config.isPluginEnable());
        Assertions.assertTrue(config.isHttpCodecEnable());
    }

    @Test
    public void configTest4() throws Exception {
        NettyConfig config = createConfig("false", "false");

        Assertions.assertFalse(config.isPluginEnable());
        Assertions.assertFalse(config.isHttpCodecEnable());
    }

    private NettyConfig createConfig(String pluginEnable, String httpEnable) {
        Properties properties = new Properties();
        properties.put(NettyConfig.PLUGIN_ENABLE, pluginEnable);
        properties.put(NettyConfig.HTTP_CODEC_ENABLE, httpEnable);

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        return new NettyConfig(profilerConfig);
    }

}
