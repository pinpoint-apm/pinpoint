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

package com.navercorp.pinpoint.profiler.context.thrift.config;

import com.navercorp.pinpoint.bootstrap.config.Value;
import com.navercorp.pinpoint.bootstrap.config.util.ValueAnnotationProcessor;

import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultThriftTransportConfig implements ThriftTransportConfig {

    private static final String DEFAULT_IP = "127.0.0.1";

    @Value("${profiler.collector.span.ip}")
    private String collectorSpanServerIp = DEFAULT_IP;
    @Value("${profiler.collector.span.port}")
    private int collectorSpanServerPort = 9996;

    @Value("${profiler.collector.stat.ip}")
    private String collectorStatServerIp = DEFAULT_IP;
    @Value("${profiler.collector.stat.port}")
    private int collectorStatServerPort = 9995;

    @Value("${profiler.collector.tcp.ip}")
    private String collectorTcpServerIp = DEFAULT_IP;
    @Value("${profiler.collector.tcp.port}")
    private int collectorTcpServerPort = 9994;

    @Value("${profiler.spandatasender.write.queue.size}")
    private int spanDataSenderWriteQueueSize = 1024 * 5;
    @Value("${profiler.spandatasender.socket.sendbuffersize}")
    private int spanDataSenderSocketSendBufferSize = 1024 * 64 * 16;
    @Value("${profiler.spandatasender.socket.timeout}")
    private int spanDataSenderSocketTimeout = 1000 * 3;
    @Value("${profiler.spandatasender.chunk.size}")
    private int spanDataSenderChunkSize = 1024 * 16;
    private static final String DEFAULT_SPAN_DATA_SENDER_WRITE_BUFFER_HIGH_WATER_MAK = "16m";
    @Value("${profiler.spandatasender.write.buffer.highwatermark}")
    private String spanDataSenderWriteBufferHighWaterMark = DEFAULT_SPAN_DATA_SENDER_WRITE_BUFFER_HIGH_WATER_MAK;
    private static final String DEFAULT_SPAN_DATA_SENDER_WRITE_BUFFER_LOW_WATER_MAK = "8m";
    @Value("${profiler.spandatasender.write.buffer.lowwatermark}")
    private String spanDataSenderWriteBufferLowWaterMark = DEFAULT_SPAN_DATA_SENDER_WRITE_BUFFER_LOW_WATER_MAK;
    @Value("${profiler.spandatasender.transport.type}")
    private String spanDataSenderTransportType = "UDP";
    @Value("${profiler.spandatasender.socket.type}")
    private String spanDataSenderSocketType = "OIO";

    @Value("${profiler.statdatasender.write.queue.size}")
    private int statDataSenderWriteQueueSize = 1024 * 5;
    @Value("${profiler.statdatasender.socket.sendbuffersize}")
    private int statDataSenderSocketSendBufferSize = 1024 * 64 * 16;
    @Value("${profiler.statdatasender.socket.timeout}")
    private int statDataSenderSocketTimeout = 1000 * 3;
    @Value("${profiler.statdatasender.chunk.size}")
    private int statDataSenderChunkSize = 1024 * 16;

    private static final String DEFAULT_STAT_DATA_SENDER_WRITE_BUFFER_HIGH_WATER_MAK = "16m";
    @Value("${profiler.statdatasender.write.buffer.highwatermark}")
    private String statDataSenderWriteBufferHighWaterMark = DEFAULT_STAT_DATA_SENDER_WRITE_BUFFER_HIGH_WATER_MAK;

    private static final String DEFAULT_STAT_DATA_SENDER_WRITE_BUFFER_LOW_WATER_MAK = "8m";
    @Value("${profiler.statdatasender.write.buffer.lowwatermark}")
    private String statDataSenderWriteBufferLowWaterMark = DEFAULT_STAT_DATA_SENDER_WRITE_BUFFER_LOW_WATER_MAK;
    @Value("${profiler.statdatasender.transport.type}")
    private String statDataSenderTransportType = "UDP";
    @Value("${profiler.statdatasender.socket.type}")
    private String statDataSenderSocketType = "OIO";

    @Value("${profiler.tcpdatasender.command.accept.enable}")
    private boolean tcpDataSenderCommandAcceptEnable = false;
    @Value("${profiler.tcpdatasender.command.activethread.enable}")
    private boolean tcpDataSenderCommandActiveThreadEnable = false;
    @Value("${profiler.tcpdatasender.command.activethread.count.enable}")
    private boolean tcpDataSenderCommandActiveThreadCountEnable = false;
    @Value("${profiler.tcpdatasender.command.activethread.threaddump.enable}")
    private boolean tcpDataSenderCommandActiveThreadDumpEnable = false;
    @Value("${profiler.tcpdatasender.command.activethread.threadlightdump.enable}")
    private boolean tcpDataSenderCommandActiveThreadLightDumpEnable = false;

    private static final long DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_TIMEOUT = 3 * 1000;
    @Value("${profiler.tcpdatasender.client.write.timeout}")
    private long tcpDataSenderPinpointClientWriteTimeout = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_TIMEOUT;

    private static final long DEFAULT_DATA_SENDER_PINPOINT_CLIENT_REQUEST_TIMEOUT = 3 * 1000;
    @Value("${profiler.tcpdatasender.client.request.timeout}")
    private long tcpDataSenderPinpointClientRequestTimeout = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_REQUEST_TIMEOUT;

    private static final long DEFAULT_DATA_SENDER_PINPOINT_CLIENT_RECONNECT_INTERVAL = 3 * 1000;
    @Value("${profiler.tcpdatasender.client.reconnect.interval}")
    private long tcpDataSenderPinpointClientReconnectInterval = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_RECONNECT_INTERVAL;

    private static final long DEFAULT_DATA_SENDER_PINPOINT_CLIENT_PING_INTERVAL = 60 * 1000 * 5;
    @Value("${profiler.tcpdatasender.client.ping.interval}")
    private long tcpDataSenderPinpointClientPingInterval = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_PING_INTERVAL;

    private static final long DEFAULT_DATA_SENDER_PINPOINT_CLIENT_HANDSHAKE_INTERVAL = 60 * 1000 * 1;
    @Value("${profiler.tcpdatasender.client.handshake.interval}")
    private long tcpDataSenderPinpointClientHandshakeInterval = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_HANDSHAKE_INTERVAL;

    private static final String DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_BUFFER_HIGH_WATER_MAK = "32m";
    @Value("${profiler.tcpdatasender.client.write.buffer.highwatermark}")
    private String tcpDataSenderPinpointClientWriteBufferHighWaterMark = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_BUFFER_HIGH_WATER_MAK;

    private static final String DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_BUFFER_LOW_WATER_MAK = "16m";
    @Value("${profiler.tcpdatasender.client.write.buffer.lowwatermark}")
    private String tcpDataSenderPinpointClientWriteBufferLowWaterMark = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_BUFFER_LOW_WATER_MAK;

    public DefaultThriftTransportConfig() {
    }

    public void read(Properties properties) {
        final ValueAnnotationProcessor reader = new ValueAnnotationProcessor();
        reader.process(this, properties);
    }

    @Override
    public String getCollectorSpanServerIp() {
        return collectorSpanServerIp;
    }

    @Override
    public int getCollectorSpanServerPort() {
        return collectorSpanServerPort;
    }

    @Override
    public String getCollectorStatServerIp() {
        return collectorStatServerIp;
    }

    @Override
    public int getCollectorStatServerPort() {
        return collectorStatServerPort;
    }

    @Override
    public String getCollectorTcpServerIp() {
        return collectorTcpServerIp;
    }

    @Override
    public int getCollectorTcpServerPort() {
        return collectorTcpServerPort;
    }

    @Override
    public int getStatDataSenderWriteQueueSize() {
        return statDataSenderWriteQueueSize;
    }

    @Override
    public int getStatDataSenderSocketSendBufferSize() {
        return statDataSenderSocketSendBufferSize;
    }

    @Override
    public int getStatDataSenderSocketTimeout() {
        return statDataSenderSocketTimeout;
    }

    @Override
    public String getStatDataSenderWriteBufferHighWaterMark() {
        return statDataSenderWriteBufferHighWaterMark;
    }

    @Override
    public String getStatDataSenderWriteBufferLowWaterMark() {
        return statDataSenderWriteBufferLowWaterMark;
    }

    @Override
    public String getStatDataSenderSocketType() {
        return statDataSenderSocketType;
    }

    @Override
    public String getStatDataSenderTransportType() {
        return statDataSenderTransportType;
    }

    @Override
    public int getSpanDataSenderWriteQueueSize() {
        return spanDataSenderWriteQueueSize;
    }

    @Override
    public int getSpanDataSenderSocketSendBufferSize() {
        return spanDataSenderSocketSendBufferSize;
    }

    @Override
    public boolean isTcpDataSenderCommandAcceptEnable() {
        return tcpDataSenderCommandAcceptEnable;
    }

    @Override
    public boolean isTcpDataSenderCommandActiveThreadEnable() {
        return tcpDataSenderCommandActiveThreadEnable;
    }

    @Override
    public boolean isTcpDataSenderCommandActiveThreadCountEnable() {
        return tcpDataSenderCommandActiveThreadCountEnable;
    }

    @Override
    public boolean isTcpDataSenderCommandActiveThreadDumpEnable() {
        return tcpDataSenderCommandActiveThreadDumpEnable;
    }

    @Override
    public boolean isTcpDataSenderCommandActiveThreadLightDumpEnable() {
        return tcpDataSenderCommandActiveThreadLightDumpEnable;
    }

    @Override
    public long getTcpDataSenderPinpointClientWriteTimeout() {
        return tcpDataSenderPinpointClientWriteTimeout;
    }

    @Override
    public long getTcpDataSenderPinpointClientRequestTimeout() {
        return tcpDataSenderPinpointClientRequestTimeout;
    }

    @Override
    public long getTcpDataSenderPinpointClientReconnectInterval() {
        return tcpDataSenderPinpointClientReconnectInterval;
    }

    @Override
    public long getTcpDataSenderPinpointClientPingInterval() {
        return tcpDataSenderPinpointClientPingInterval;
    }

    @Override
    public long getTcpDataSenderPinpointClientHandshakeInterval() {
        return tcpDataSenderPinpointClientHandshakeInterval;
    }

    @Override
    public String getTcpDataSenderPinpointClientWriteBufferHighWaterMark() {
        return tcpDataSenderPinpointClientWriteBufferHighWaterMark;
    }

    @Override
    public String getTcpDataSenderPinpointClientWriteBufferLowWaterMark() {
        return tcpDataSenderPinpointClientWriteBufferLowWaterMark;
    }

    @Override
    public int getSpanDataSenderSocketTimeout() {
        return spanDataSenderSocketTimeout;
    }

    @Override
    public String getSpanDataSenderSocketType() {
        return spanDataSenderSocketType;
    }

    @Override
    public String getSpanDataSenderTransportType() {
        return spanDataSenderTransportType;
    }

    @Override
    public String getSpanDataSenderWriteBufferHighWaterMark() {
        return spanDataSenderWriteBufferHighWaterMark;
    }

    @Override
    public String getSpanDataSenderWriteBufferLowWaterMark() {
        return spanDataSenderWriteBufferLowWaterMark;
    }

    @Override
    public int getSpanDataSenderChunkSize() {
        return spanDataSenderChunkSize;
    }

    @Override
    public int getStatDataSenderChunkSize() {
        return statDataSenderChunkSize;
    }

    @Override
    public String toString() {
        return "DefaultThriftTransportConfig{" +
                "collectorSpanServerIp='" + collectorSpanServerIp + '\'' +
                ", collectorSpanServerPort=" + collectorSpanServerPort +
                ", collectorStatServerIp='" + collectorStatServerIp + '\'' +
                ", collectorStatServerPort=" + collectorStatServerPort +
                ", collectorTcpServerIp='" + collectorTcpServerIp + '\'' +
                ", collectorTcpServerPort=" + collectorTcpServerPort +
                ", spanDataSenderWriteQueueSize=" + spanDataSenderWriteQueueSize +
                ", spanDataSenderSocketSendBufferSize=" + spanDataSenderSocketSendBufferSize +
                ", spanDataSenderSocketTimeout=" + spanDataSenderSocketTimeout +
                ", spanDataSenderChunkSize=" + spanDataSenderChunkSize +
                ", spanDataSenderWriteBufferHighWaterMark=" + spanDataSenderWriteBufferHighWaterMark +
                ", spanDataSenderWriteBufferLowWaterMark=" + spanDataSenderWriteBufferLowWaterMark +
                ", spanDataSenderTransportType='" + spanDataSenderTransportType + '\'' +
                ", spanDataSenderSocketType='" + spanDataSenderSocketType + '\'' +
                ", statDataSenderWriteQueueSize=" + statDataSenderWriteQueueSize +
                ", statDataSenderSocketSendBufferSize=" + statDataSenderSocketSendBufferSize +
                ", statDataSenderSocketTimeout=" + statDataSenderSocketTimeout +
                ", statDataSenderChunkSize=" + statDataSenderChunkSize +
                ", statDataSenderWriteBufferHighWaterMark=" + statDataSenderWriteBufferHighWaterMark +
                ", statDataSenderWriteBufferLowWaterMark=" + statDataSenderWriteBufferLowWaterMark +
                ", statDataSenderTransportType='" + statDataSenderTransportType + '\'' +
                ", statDataSenderSocketType='" + statDataSenderSocketType + '\'' +
                ", tcpDataSenderCommandAcceptEnable=" + tcpDataSenderCommandAcceptEnable +
                ", tcpDataSenderCommandActiveThreadEnable=" + tcpDataSenderCommandActiveThreadEnable +
                ", tcpDataSenderCommandActiveThreadCountEnable=" + tcpDataSenderCommandActiveThreadCountEnable +
                ", tcpDataSenderCommandActiveThreadDumpEnable=" + tcpDataSenderCommandActiveThreadDumpEnable +
                ", tcpDataSenderCommandActiveThreadLightDumpEnable=" + tcpDataSenderCommandActiveThreadLightDumpEnable +
                ", tcpDataSenderPinpointClientWriteTimeout=" + tcpDataSenderPinpointClientWriteTimeout +
                ", tcpDataSenderPinpointClientRequestTimeout=" + tcpDataSenderPinpointClientRequestTimeout +
                ", tcpDataSenderPinpointClientReconnectInterval=" + tcpDataSenderPinpointClientReconnectInterval +
                ", tcpDataSenderPinpointClientPingInterval=" + tcpDataSenderPinpointClientPingInterval +
                ", tcpDataSenderPinpointClientHandshakeInterval=" + tcpDataSenderPinpointClientHandshakeInterval +
                ", tcpDataSenderPinpointClientWriteBufferHighWaterMark=" + tcpDataSenderPinpointClientWriteBufferHighWaterMark +
                ", tcpDataSenderPinpointClientWriteBufferLowWaterMark=" + tcpDataSenderPinpointClientWriteBufferLowWaterMark +
                '}';
    }
}
