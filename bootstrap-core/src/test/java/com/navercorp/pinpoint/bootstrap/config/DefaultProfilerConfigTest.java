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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

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
        ThriftTransportConfig thriftTransportConfig = profilerConfig.getThriftTransportConfig();
        Assert.assertEquals(thriftTransportConfig.getCollectorSpanServerIp(), "placeHolder1");
        Assert.assertEquals(thriftTransportConfig.getCollectorStatServerIp(), "placeHolder1");
        Assert.assertEquals(thriftTransportConfig.getCollectorTcpServerIp(), "placeHolder2");
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
        ThriftTransportConfig thriftTransportConfig = profilerConfig.getThriftTransportConfig();
        Assert.assertFalse(thriftTransportConfig.isTcpDataSenderCommandAcceptEnable());
    }

    @Test
    public void tcpCommandAcceptorConfigTest2() throws IOException {
        String path = DefaultProfilerConfig.class.getResource("/com/navercorp/pinpoint/bootstrap/config/test2.property").getPath();
        logger.debug("path:{}", path);

        ProfilerConfig profilerConfig = DefaultProfilerConfig.load(path);
        ThriftTransportConfig thriftTransportConfig = profilerConfig.getThriftTransportConfig();
        Assert.assertTrue(thriftTransportConfig.isTcpDataSenderCommandAcceptEnable());
    }

    @Test
    public void getCallStackMaxDepth() {
        Properties properties = new Properties();
        properties.setProperty("profiler.callstack.max.depth", "64");

        // Read
        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        Assert.assertEquals(profilerConfig.getCallStackMaxDepth(), 64);

        // Unlimited
        properties.setProperty("profiler.callstack.max.depth", "-1");
        profilerConfig = new DefaultProfilerConfig(properties);
        Assert.assertEquals(profilerConfig.getCallStackMaxDepth(), -1);
        // Minimum calibration
        properties.setProperty("profiler.callstack.max.depth", "0");
        profilerConfig = new DefaultProfilerConfig(properties);
        Assert.assertEquals(profilerConfig.getCallStackMaxDepth(), 2);
    }

    @Test
    public void waterMarkConfigTest() {
        Properties properties = new Properties();
        properties.setProperty("profiler.tcpdatasender.client.write.buffer.highwatermark", "6m");
        properties.setProperty("profiler.tcpdatasender.client.write.buffer.lowwatermark", "5m");
        properties.setProperty("profiler.spandatasender.write.buffer.highwatermark", "4m");
        properties.setProperty("profiler.spandatasender.write.buffer.lowwatermark", "3m");
        properties.setProperty("profiler.statdatasender.write.buffer.highwatermark", "2m");
        properties.setProperty("profiler.statdatasender.write.buffer.lowwatermark", "1m");

        ProfilerConfig profilerConfig = new DefaultProfilerConfig(properties);
        ThriftTransportConfig thriftTransportConfig = profilerConfig.getThriftTransportConfig();
        Assert.assertEquals("6m", thriftTransportConfig.getTcpDataSenderPinpointClientWriteBufferHighWaterMark());
        Assert.assertEquals("5m", thriftTransportConfig.getTcpDataSenderPinpointClientWriteBufferLowWaterMark());
        Assert.assertEquals("4m", thriftTransportConfig.getSpanDataSenderWriteBufferHighWaterMark());
        Assert.assertEquals("3m", thriftTransportConfig.getSpanDataSenderWriteBufferLowWaterMark());
        Assert.assertEquals("2m", thriftTransportConfig.getStatDataSenderWriteBufferHighWaterMark());
        Assert.assertEquals("1m", thriftTransportConfig.getStatDataSenderWriteBufferLowWaterMark());
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
