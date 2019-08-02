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

package com.navercorp.pinpoint.bootstrap.config;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultThriftTransportConfig implements ThriftTransportConfig {

    private static final String DEFAULT_IP = "127.0.0.1";

    private String collectorSpanServerIp = DEFAULT_IP;
    private int collectorSpanServerPort = 9996;

    private String collectorStatServerIp = DEFAULT_IP;
    private int collectorStatServerPort = 9995;

    private String collectorTcpServerIp = DEFAULT_IP;
    private int collectorTcpServerPort = 9994;

    private int spanDataSenderWriteQueueSize = 1024 * 5;
    private int spanDataSenderSocketSendBufferSize = 1024 * 64 * 16;
    private int spanDataSenderSocketTimeout = 1000 * 3;
    private int spanDataSenderChunkSize = 1024 * 16;
    private static String DEFAULT_SPAN_DATA_SENDER_WRITE_BUFFER_HIGH_WATER_MAK = "16m";
    private String spanDataSenderWriteBufferHighWaterMark = DEFAULT_SPAN_DATA_SENDER_WRITE_BUFFER_HIGH_WATER_MAK;
    private static String DEFAULT_SPAN_DATA_SENDER_WRITE_BUFFER_LOW_WATER_MAK = "8m";
    private String spanDataSenderWriteBufferLowWaterMark = DEFAULT_SPAN_DATA_SENDER_WRITE_BUFFER_LOW_WATER_MAK;
    private String spanDataSenderTransportType = "UDP";
    private String spanDataSenderSocketType = "OIO";

    private int statDataSenderWriteQueueSize = 1024 * 5;
    private int statDataSenderSocketSendBufferSize = 1024 * 64 * 16;
    private int statDataSenderSocketTimeout = 1000 * 3;
    private int statDataSenderChunkSize = 1024 * 16;
    private static String DEFAULT_STAT_DATA_SENDER_WRITE_BUFFER_HIGH_WATER_MAK = "16m";
    private String statDataSenderWriteBufferHighWaterMark = DEFAULT_STAT_DATA_SENDER_WRITE_BUFFER_HIGH_WATER_MAK;
    private static String DEFAULT_STAT_DATA_SENDER_WRITE_BUFFER_LOW_WATER_MAK = "8m";
    private String statDataSenderWriteBufferLowWaterMark = DEFAULT_STAT_DATA_SENDER_WRITE_BUFFER_LOW_WATER_MAK;
    private String statDataSenderTransportType = "UDP";
    private String statDataSenderSocketType = "OIO";

    private boolean tcpDataSenderCommandAcceptEnable = false;
    private boolean tcpDataSenderCommandActiveThreadEnable = false;
    private boolean tcpDataSenderCommandActiveThreadCountEnable = false;
    private boolean tcpDataSenderCommandActiveThreadDumpEnable = false;
    private boolean tcpDataSenderCommandActiveThreadLightDumpEnable = false;

    private static long DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_TIMEOUT = 3 * 1000;
    private long tcpDataSenderPinpointClientWriteTimeout = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_TIMEOUT;
    private static long DEFAULT_DATA_SENDER_PINPOINT_CLIENT_REQUEST_TIMEOUT = 3 * 1000;
    private long tcpDataSenderPinpointClientRequestTimeout = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_REQUEST_TIMEOUT;
    private static long DEFAULT_DATA_SENDER_PINPOINT_CLIENT_RECONNECT_INTERVAL = 3 * 1000;
    private long tcpDataSenderPinpointClientReconnectInterval = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_RECONNECT_INTERVAL;
    private static long DEFAULT_DATA_SENDER_PINPOINT_CLIENT_PING_INTERVAL = 60 * 1000 * 5;
    private long tcpDataSenderPinpointClientPingInterval = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_PING_INTERVAL;
    private static long DEFAULT_DATA_SENDER_PINPOINT_CLIENT_HANDSHAKE_INTERVAL = 60 * 1000 * 1;
    private long tcpDataSenderPinpointClientHandshakeInterval = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_HANDSHAKE_INTERVAL;
    private static String DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_BUFFER_HIGH_WATER_MAK = "32m";
    private String tcpDataSenderPinpointClientWriteBufferHighWaterMark = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_BUFFER_HIGH_WATER_MAK;
    private static String DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_BUFFER_LOW_WATER_MAK = "16m";
    private String tcpDataSenderPinpointClientWriteBufferLowWaterMark = DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_BUFFER_LOW_WATER_MAK;

    public DefaultThriftTransportConfig() {
    }

    public void read(DefaultProfilerConfig profilerConfig) {
        final DefaultProfilerConfig.ValueResolver placeHolderResolver = new DefaultProfilerConfig.PlaceHolderResolver();
        this.collectorSpanServerIp = profilerConfig.readString("profiler.collector.span.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorSpanServerPort = profilerConfig.readInt("profiler.collector.span.port", 9996);

        this.collectorStatServerIp = profilerConfig.readString("profiler.collector.stat.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorStatServerPort = profilerConfig.readInt("profiler.collector.stat.port", 9995);

        this.collectorTcpServerIp = profilerConfig.readString("profiler.collector.tcp.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorTcpServerPort = profilerConfig.readInt("profiler.collector.tcp.port", 9994);

        this.spanDataSenderWriteQueueSize = profilerConfig.readInt("profiler.spandatasender.write.queue.size", 1024 * 5);
        this.spanDataSenderSocketSendBufferSize = profilerConfig.readInt("profiler.spandatasender.socket.sendbuffersize", 1024 * 64 * 16);
        this.spanDataSenderSocketTimeout = profilerConfig.readInt("profiler.spandatasender.socket.timeout", 1000 * 3);
        this.spanDataSenderChunkSize = profilerConfig.readInt("profiler.spandatasender.chunk.size", 1024 * 16);
        this.spanDataSenderWriteBufferHighWaterMark = profilerConfig.readString("profiler.spandatasender.write.buffer.highwatermark", DEFAULT_SPAN_DATA_SENDER_WRITE_BUFFER_HIGH_WATER_MAK);
        this.spanDataSenderWriteBufferLowWaterMark = profilerConfig.readString("profiler.spandatasender.write.buffer.lowwatermark", DEFAULT_SPAN_DATA_SENDER_WRITE_BUFFER_LOW_WATER_MAK);
       this.spanDataSenderSocketType = profilerConfig.readString("profiler.spandatasender.socket.type", "OIO");
        this.spanDataSenderTransportType = profilerConfig.readString("profiler.spandatasender.transport.type", "UDP");

        this.statDataSenderWriteQueueSize = profilerConfig.readInt("profiler.statdatasender.write.queue.size", 1024 * 5);
        this.statDataSenderSocketSendBufferSize = profilerConfig.readInt("profiler.statdatasender.socket.sendbuffersize", 1024 * 64 * 16);
        this.statDataSenderSocketTimeout = profilerConfig.readInt("profiler.statdatasender.socket.timeout", 1000 * 3);
        this.statDataSenderChunkSize = profilerConfig.readInt("profiler.statdatasender.chunk.size", 1024 * 16);
        this.statDataSenderWriteBufferHighWaterMark = profilerConfig.readString("profiler.statdatasender.write.buffer.highwatermark", DEFAULT_STAT_DATA_SENDER_WRITE_BUFFER_HIGH_WATER_MAK);
        this.statDataSenderWriteBufferLowWaterMark = profilerConfig.readString("profiler.statdatasender.write.buffer.lowwatermark", DEFAULT_STAT_DATA_SENDER_WRITE_BUFFER_LOW_WATER_MAK);
        this.statDataSenderSocketType = profilerConfig.readString("profiler.statdatasender.socket.type", "OIO");
        this.statDataSenderTransportType = profilerConfig.readString("profiler.statdatasender.transport.type", "UDP");

        this.tcpDataSenderCommandAcceptEnable = profilerConfig.readBoolean("profiler.tcpdatasender.command.accept.enable", false);
        this.tcpDataSenderCommandActiveThreadEnable = profilerConfig.readBoolean("profiler.tcpdatasender.command.activethread.enable", false);
        this.tcpDataSenderCommandActiveThreadCountEnable = profilerConfig.readBoolean("profiler.tcpdatasender.command.activethread.count.enable", false);
        this.tcpDataSenderCommandActiveThreadDumpEnable = profilerConfig.readBoolean("profiler.tcpdatasender.command.activethread.threaddump.enable", false);
        this.tcpDataSenderCommandActiveThreadLightDumpEnable = profilerConfig.readBoolean("profiler.tcpdatasender.command.activethread.threadlightdump.enable", false);

        this.tcpDataSenderPinpointClientWriteTimeout = profilerConfig.readLong("profiler.tcpdatasender.client.write.timeout", DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_TIMEOUT);
        this.tcpDataSenderPinpointClientRequestTimeout = profilerConfig.readLong("profiler.tcpdatasender.client.request.timeout", DEFAULT_DATA_SENDER_PINPOINT_CLIENT_REQUEST_TIMEOUT);
        this.tcpDataSenderPinpointClientReconnectInterval = profilerConfig.readLong("profiler.tcpdatasender.client.reconnect.interval", DEFAULT_DATA_SENDER_PINPOINT_CLIENT_RECONNECT_INTERVAL);
        this.tcpDataSenderPinpointClientPingInterval = profilerConfig.readLong("profiler.tcpdatasender.client.ping.interval", DEFAULT_DATA_SENDER_PINPOINT_CLIENT_PING_INTERVAL);
        this.tcpDataSenderPinpointClientHandshakeInterval = profilerConfig.readLong("profiler.tcpdatasender.client.handshake.interval", DEFAULT_DATA_SENDER_PINPOINT_CLIENT_HANDSHAKE_INTERVAL);
        this.tcpDataSenderPinpointClientWriteBufferHighWaterMark = profilerConfig.readString("profiler.tcpdatasender.client.write.buffer.highwatermark", DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_BUFFER_HIGH_WATER_MAK);
        this.tcpDataSenderPinpointClientWriteBufferLowWaterMark = profilerConfig.readString("profiler.tcpdatasender.client.write.buffer.lowwatermark", DEFAULT_DATA_SENDER_PINPOINT_CLIENT_WRITE_BUFFER_LOW_WATER_MAK);
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
