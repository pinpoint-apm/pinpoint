/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * @author jaehong.kim
 */
public class ServerConfigTest {

    @Test
    public void isHidePinpointHeader() {
        final String propertyName = "profiler.tomcat.hidepinpointheader";
        Properties properties = new Properties();
        properties.setProperty(ServerConfig.HIDE_PINPOINT_HEADER_PROPERTY_NAME, "true");
        properties.setProperty(propertyName, "false");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        ServerConfig serverConfig = new ServerConfig(profilerConfig);

        Assert.assertFalse(serverConfig.isHidePinpointHeader(propertyName));
    }

    @Test
    public void isTraceRequestParam() {
        final String propertyName = "profiler.tomcat.tracerequestparam";
        Properties properties = new Properties();
        properties.setProperty(ServerConfig.TRACE_REQUEST_PARAM_PROPERTY_NAME, "true");
        properties.setProperty(propertyName, "false");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        ServerConfig serverConfig = new ServerConfig(profilerConfig);

        Assert.assertFalse(serverConfig.isTraceRequestParam(propertyName));
    }

    @Test
    public void getExcludeUrlFilter() {
        final String propertyName = "profiler.tomcat.excludeurl";
        Properties properties = new Properties();
        properties.setProperty(ServerConfig.EXCLUDE_URL_PROPERTY_NAME, "/l7check");
        properties.setProperty(propertyName, "/healthcheck");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        ServerConfig serverConfig = new ServerConfig(profilerConfig);

        Filter<String> filter = serverConfig.getExcludeUrlFilter(propertyName);
        Assert.assertTrue(filter.filter("/healthcheck"));
        Assert.assertFalse(filter.filter("/l7check"));
    }

    @Test
    public void getRealIpHeader() {
        final String propertyName = "profiler.tomcat.realipheader";
        Properties properties = new Properties();
        properties.setProperty(ServerConfig.REAL_IP_HEADER_PROPERTY_NAME, "X-Forwarded-For");
        properties.setProperty(propertyName, "");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        ServerConfig serverConfig = new ServerConfig(profilerConfig);

        Assert.assertEquals("X-Forwarded-For", serverConfig.getRealIpHeader(propertyName));
    }

    @Test
    public void getRealIpEmptyValue() {
        final String propertyName = "profiler.tomcat.realipemptyvalue";
        Properties properties = new Properties();
        properties.setProperty(ServerConfig.REAL_IP_HEADER_PROPERTY_NAME, "");
        properties.setProperty(propertyName, "UNKNOWN");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        ServerConfig serverConfig = new ServerConfig(profilerConfig);

        Assert.assertEquals("UNKNOWN", serverConfig.getRealIpEmptyValue(propertyName));
    }

    @Test
    public void getExcludeMethodFilter() {
        final String propertyName = "profiler.tomcat.excludemethod";
        Properties properties = new Properties();
        properties.setProperty(ServerConfig.EXCLUDE_METHOD_PROPERTY_NAME, "POST");
        properties.setProperty(propertyName, "HEAD");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        ServerConfig serverConfig = new ServerConfig(profilerConfig);

        Filter<String> filter = serverConfig.getExcludeMethodFilter(propertyName);
        Assert.assertTrue(filter.filter("HEAD"));
        Assert.assertFalse(filter.filter("POST"));
    }
}