/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class GrpcClientConfigTest {

    @Test
    public void configTest1() throws Exception {
        GrpcClientConfig config = createConfig("false", "false");

        Assert.assertFalse(config.isClientEnable());
    }

    @Test
    public void configTest2() throws Exception {
        GrpcClientConfig config = createConfig("false", "true");

        Assert.assertFalse(config.isClientEnable());
    }

    @Test
    public void configTest3() throws Exception {
        GrpcClientConfig config = createConfig("true", "false");

        Assert.assertTrue(config.isClientEnable());
    }

    @Test
    public void configTest4() throws Exception {
        GrpcClientConfig config = createConfig("true", "true");

        Assert.assertTrue(config.isClientEnable());
    }

    private GrpcClientConfig createConfig(String clientEnable, String serverEnable) {
        Properties properties = new Properties();
        properties.put(GrpcClientConfig.CLIENT_ENABLE, clientEnable);

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);

        return new GrpcClientConfig(profilerConfig);
    }

}
