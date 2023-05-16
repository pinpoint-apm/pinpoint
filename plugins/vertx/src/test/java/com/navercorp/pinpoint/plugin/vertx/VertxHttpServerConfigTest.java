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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jaehong.kim
 */
public class VertxHttpServerConfigTest {

    @Test
    public void config() {
        Properties properties = new Properties();
        properties.setProperty("profiler.vertx.http.server.tracerequestparam", "true");
        properties.setProperty("profiler.vertx.http.server.excludeurl", "/l7/check");
        properties.setProperty("profiler.vertx.http.server.realipheader", "RealIp");
        properties.setProperty("profiler.vertx.http.server.realipemptyvalue", "unknown");
        properties.setProperty("profiler.vertx.http.server.excludemethod", "chunk, continue");

        ProfilerConfig profilerConfig = ProfilerConfigLoader.load(properties);
        VertxHttpServerConfig config = new VertxHttpServerConfig(profilerConfig);

        assertEquals(true, config.isTraceRequestParam());
        assertEquals(true, config.getExcludeUrlFilter().filter("/l7/check"));
        assertEquals("RealIp", config.getRealIpHeader());
        assertEquals("unknown", config.getRealIpEmptyValue());
        assertEquals(true, config.getExcludeProfileMethodFilter().filter("CHUNK"));

        properties = new Properties();
        properties.setProperty("profiler.vertx.http.server.tracerequestparam", "false");
        properties.setProperty("profiler.vertx.http.server.excludeurl", "");
        properties.setProperty("profiler.vertx.http.server.realipheader", "");
        properties.setProperty("profiler.vertx.http.server.realipemptyvalue", "");
        properties.setProperty("profiler.vertx.http.server.excludemethod", "");

        profilerConfig = ProfilerConfigLoader.load(properties);
        config = new VertxHttpServerConfig(profilerConfig);

        assertEquals(false, config.isTraceRequestParam());
        assertEquals(false, config.getExcludeUrlFilter().filter("/l7/check"));
        assertEquals("", config.getRealIpHeader());
        assertEquals("", config.getRealIpEmptyValue());
        assertEquals(false, config.getExcludeProfileMethodFilter().filter("CHUNK"));

        properties = new Properties();
        properties.setProperty("profiler.vertx.http.server.tracerequestparam", "true");
        properties.setProperty("profiler.server.tracerequestparam", "false");
        properties.setProperty("profiler.vertx.http.server.excludeurl", "/l7/check");
        properties.setProperty("profiler.server.excludeurl", "/health");
        properties.setProperty("profiler.vertx.http.server.realipheader", "RealIp");
        properties.setProperty("profiler.server.realipheader", "X-Forward-Ip");
        properties.setProperty("profiler.vertx.http.server.realipemptyvalue", "unknown");
        properties.setProperty("profiler.server.realipemptyvalue", "UFO");
        properties.setProperty("profiler.vertx.http.server.excludemethod", "chunk, continue");
        properties.setProperty("profiler.server.excludemethod", "POST, PUT");

    }
}