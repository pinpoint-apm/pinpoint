/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jaehong.kim
 */
public class VertxConfigTest {

    @Test
    public void config() {
        Properties properties = new Properties();
        properties.setProperty("profiler.vertx.enable", "true");
        properties.setProperty("profiler.vertx.http.server.enable", "true");
        properties.setProperty("profiler.vertx.http.client.enable", "true");
        properties.setProperty("profiler.vertx.bootstrap.main", "io.vertx.core.Starter");

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);
        VertxConfig config = new VertxConfig(profilerConfig);
        assertEquals(true, config.isEnable());
        assertEquals(true, config.isEnableHttpServer());
        assertEquals(true, config.isEnableHttpClient());
        assertThat(config.getBootstrapMains()).hasSize(1);
        assertEquals("io.vertx.core.Starter", config.getBootstrapMains().get(0));

        properties = new Properties();
        properties.setProperty("profiler.vertx.enable", "false");
        properties.setProperty("profiler.vertx.http.server.enable", "false");
        properties.setProperty("profiler.vertx.http.client.enable", "false");
        properties.setProperty("profiler.vertx.bootstrap.main", "");

        profilerConfig = ProfilerConfigLoader.load(properties);
        config = new VertxConfig(profilerConfig);
        assertEquals(false, config.isEnable());
        assertEquals(false, config.isEnableHttpServer());
        assertEquals(false, config.isEnableHttpClient());
        assertThat(config.getBootstrapMains()).isEmpty();
    }
}