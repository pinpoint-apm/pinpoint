/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.config;

import java.io.IOException;
import java.util.Properties;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class DefaultProfilerConfigTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void defaultProfilableClassFilter() throws IOException {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        Filter<String> profilableClassFilter = profilerConfig.getProfilableClassFilter();
        Assert.assertFalse(profilableClassFilter.filter("net/spider/king/wang/Jjang"));
    }

    @Test
    public void readProperty() throws IOException {
        String path = DefaultProfilerConfig.class.getResource("/com/navercorp/pinpoint/bootstrap/config/test.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = DefaultProfilerConfig.load(path);
    }

    @Test
    public void testPlaceHolder() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("profiler.collector.span.ip", "${test1}");
        properties.setProperty("profiler.collector.stat.ip", "${test1}");
        properties.setProperty("profiler.collector.tcp.ip", "${test2}");
        // placeHolderValue
        properties.setProperty("test1", "placeHolder1");
        properties.setProperty("test2", "placeHolder2");


        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);

        Assert.assertEquals(profilerConfig.getCollectorSpanServerIp(), "placeHolder1");
        Assert.assertEquals(profilerConfig.getCollectorStatServerIp(), "placeHolder1");
        Assert.assertEquals(profilerConfig.getCollectorTcpServerIp(), "placeHolder2");
    }


    @Test
    public void ioBuffering_test() throws IOException {
        String path = DefaultProfilerConfig.class.getResource("/com/navercorp/pinpoint/bootstrap/config/test.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = DefaultProfilerConfig.load(path);

        Assert.assertEquals(profilerConfig.isIoBufferingEnable(), false);
        Assert.assertEquals(profilerConfig.getIoBufferingBufferSize(), 30);
    }

    @Test
    public void ioBuffering_default() throws IOException {
        String path = DefaultProfilerConfig.class.getResource("/com/navercorp/pinpoint/bootstrap/config/default.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = DefaultProfilerConfig.load(path);

        Assert.assertEquals(profilerConfig.isIoBufferingEnable(), true);
        Assert.assertEquals(profilerConfig.getIoBufferingBufferSize(), 10);
    }
    
    @Test
    public void tcpCommandAcceptorConfigTest1() throws IOException {
        String path = DefaultProfilerConfig.class.getResource("/com/navercorp/pinpoint/bootstrap/config/test.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = DefaultProfilerConfig.load(path);
        
        Assert.assertFalse(profilerConfig.isTcpDataSenderCommandAcceptEnable());
    }
    
    @Test
    public void tcpCommandAcceptorConfigTest2() throws IOException {
        String path = DefaultProfilerConfig.class.getResource("/com/navercorp/pinpoint/bootstrap/config/test2.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = DefaultProfilerConfig.load(path);
        
        Assert.assertTrue(profilerConfig.isTcpDataSenderCommandAcceptEnable());
    }

    @Test
    public void proxyHttpHeaders() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("profiler.proxy.http.header.names", "PINPOINT_PROXY, PROXY1");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        System.out.println(profilerConfig.getProxyHttpHeaderNames());
        Assert.assertThat(profilerConfig.getProxyHttpHeaderNames(), CoreMatchers.hasItems("PINPOINT_PROXY", "PROXY1"));
    }

    @Test
    public void readList() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("profiler.test.list1", "foo,bar");
        properties.setProperty("profiler.test.list2", "foo, bar");
        properties.setProperty("profiler.test.list3", " foo,bar");
        properties.setProperty("profiler.test.list4", "foo,bar ");
        properties.setProperty("profiler.test.list5", "    foo,    bar   ");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);

        Assert.assertThat(profilerConfig.readList("profiler.test.list1"), CoreMatchers.hasItems("foo", "bar"));
        Assert.assertThat(profilerConfig.readList("profiler.test.list2"), CoreMatchers.hasItems("foo", "bar"));
        Assert.assertThat(profilerConfig.readList("profiler.test.list3"), CoreMatchers.hasItems("foo", "bar"));
        Assert.assertThat(profilerConfig.readList("profiler.test.list4"), CoreMatchers.hasItems("foo", "bar"));
    }
}
