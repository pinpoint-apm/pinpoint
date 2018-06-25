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

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class NettyConfigTest {

    @Test
    public void configTest1() throws Exception {
        NettyConfig config = createConfig("true", "true");

        Assert.assertTrue(config.isPluginEnable());
        Assert.assertTrue(config.isHttpCodecEnable());
    }

    @Test
    public void configTest2() throws Exception {
        NettyConfig config = createConfig("true", "false");

        Assert.assertTrue(config.isPluginEnable());
        Assert.assertFalse(config.isHttpCodecEnable());
    }

    @Test
    public void configTest3() throws Exception {
        NettyConfig config = createConfig("false", "true");

        Assert.assertFalse(config.isPluginEnable());
        Assert.assertTrue(config.isHttpCodecEnable());
    }

    @Test
    public void configTest4() throws Exception {
        NettyConfig config = createConfig("false", "false");

        Assert.assertFalse(config.isPluginEnable());
        Assert.assertFalse(config.isHttpCodecEnable());
    }

    private NettyConfig createConfig(String pluginEnable, String httpEnable) {
        Properties properties = new Properties();
        properties.put(NettyConfig.PLUGIN_ENABLE, pluginEnable);
        properties.put(NettyConfig.HTTP_CODEC_ENABLE, httpEnable);

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);

        return new NettyConfig(profilerConfig);
    }

}
