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

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class VertxHttpClientConfigTest {

    @Test
    public void config() {
        Properties properties = new Properties();
        properties.setProperty("profiler.vertx.http.client.param", "true");
        properties.setProperty("profiler.vertx.http.client.cookie", "true");
        properties.setProperty("profiler.vertx.http.client.cookie.dumptype", "EXCEPTION");
        properties.setProperty("profiler.vertx.http.client.cookie.sampling.rate", "1");
        properties.setProperty("profiler.vertx.http.client.entity.statuscode", "true");

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);
        VertxHttpClientConfig config = new VertxHttpClientConfig(profilerConfig);

        assertEquals(true, config.isParam());
        assertEquals(true, config.isStatusCode());

        properties = new Properties();
        properties.setProperty("profiler.vertx.http.client.param", "false");
        properties.setProperty("profiler.vertx.http.client.cookie", "false");
        properties.setProperty("profiler.vertx.http.client.cookie.dumptype", "ALWAYS");
        properties.setProperty("profiler.vertx.http.client.cookie.sampling.rate", "99");
        properties.setProperty("profiler.vertx.http.client.entity.statuscode", "false");

        profilerConfig = ProfilerConfigLoader.load(properties);
        config = new VertxHttpClientConfig(profilerConfig);

        assertEquals(false, config.isParam());
        assertEquals(false, config.isStatusCode());
    }
}