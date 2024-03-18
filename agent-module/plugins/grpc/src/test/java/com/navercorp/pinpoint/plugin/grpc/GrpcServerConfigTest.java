/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * @author Taejin Koo
 */
public class GrpcServerConfigTest {

    @Test
    public void configTest1() {
        GrpcServerConfig config = createConfig("false", "false");

        Assertions.assertFalse(config.isServerEnable());
    }

    @Test
    public void configTest2() {
        GrpcServerConfig config = createConfig("false", "true");

        Assertions.assertTrue(config.isServerEnable());
    }

    @Test
    public void configTest3() {
        GrpcServerConfig config = createConfig("true", "false");

        Assertions.assertFalse(config.isServerEnable());
    }

    @Test
    public void configTest4() {
        GrpcServerConfig config = createConfig("true", "true");

        Assertions.assertTrue(config.isServerEnable());
    }

    private GrpcServerConfig createConfig(String clientEnable, String serverEnable) {
        Properties properties = new Properties();
        properties.put(GrpcServerConfig.SERVER_ENABLE, serverEnable);

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);

        return new GrpcServerConfig(profilerConfig);
    }

}
