package com.navercorp.pinpoint.profiler.context.thrift.config;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class DefaultThriftTransportConfigTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testPlaceHolder() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("profiler.collector.span.ip", "${test1}");
        properties.setProperty("profiler.collector.stat.ip", "${test1}");
        properties.setProperty("profiler.collector.tcp.ip", "${test2}");
        // placeHolderValue
        properties.setProperty("test1", "placeHolder1");
        properties.setProperty("test2", "placeHolder2");


        ThriftTransportConfig thriftTransportConfig = loadThriftTransportConfig(properties);
        Assert.assertEquals(thriftTransportConfig.getCollectorSpanServerIp(), "placeHolder1");
        Assert.assertEquals(thriftTransportConfig.getCollectorStatServerIp(), "placeHolder1");
        Assert.assertEquals(thriftTransportConfig.getCollectorTcpServerIp(), "placeHolder2");
    }


    @Test
    public void tcpCommandAcceptorConfigTest1() throws IOException {
        Properties properties = new Properties();
        properties.put("profiler.tcpdatasender.command.accept.enable", "true");

        ThriftTransportConfig thriftTransportConfig = loadThriftTransportConfig(properties);
        Assert.assertTrue(thriftTransportConfig.isTcpDataSenderCommandAcceptEnable());
    }

    @Test
    public void tcpCommandAcceptorConfigTest2() throws IOException {
        Properties properties = new Properties();
        properties.put("profiler.tcpdatasender.command.accept.enable", "true");

        ThriftTransportConfig thriftTransportConfig = loadThriftTransportConfig(properties);
        Assert.assertTrue(thriftTransportConfig.isTcpDataSenderCommandAcceptEnable());
    }


    private DefaultThriftTransportConfig loadThriftTransportConfig(Properties properties) {
        DefaultThriftTransportConfig thriftTransportConfig = new DefaultThriftTransportConfig();
        thriftTransportConfig.read(properties);
        return thriftTransportConfig;
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

        DefaultThriftTransportConfig thriftTransportConfig = new DefaultThriftTransportConfig();
        thriftTransportConfig.read(properties);
        Assert.assertEquals("6m", thriftTransportConfig.getTcpDataSenderPinpointClientWriteBufferHighWaterMark());
        Assert.assertEquals("5m", thriftTransportConfig.getTcpDataSenderPinpointClientWriteBufferLowWaterMark());
        Assert.assertEquals("4m", thriftTransportConfig.getSpanDataSenderWriteBufferHighWaterMark());
        Assert.assertEquals("3m", thriftTransportConfig.getSpanDataSenderWriteBufferLowWaterMark());
        Assert.assertEquals("2m", thriftTransportConfig.getStatDataSenderWriteBufferHighWaterMark());
        Assert.assertEquals("1m", thriftTransportConfig.getStatDataSenderWriteBufferLowWaterMark());
    }
}