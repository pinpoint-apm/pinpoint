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

package com.navercorp.pinpoint.grpc.client.config;

import com.navercorp.pinpoint.bootstrap.config.Value;
import com.navercorp.pinpoint.bootstrap.module.JavaModule;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.ByteSizeUnit;
import com.navercorp.pinpoint.grpc.ChannelTypeEnum;

import java.util.concurrent.TimeUnit;

/**
 * NOTE module accessibility
 * @see com.navercorp.pinpoint.bootstrap.java9.module.ModuleSupport#addPermissionToValueAnnotation(JavaModule)
 * @author jaehong.kim
 */
public class ClientOption {
    public static final long DEFAULT_KEEPALIVE_TIME = TimeUnit.SECONDS.toMillis(30); // 30 seconds
    public static final long DEFAULT_KEEPALIVE_TIMEOUT = TimeUnit.SECONDS.toMillis(60); // 60 seconds
    public static final long IDLE_TIMEOUT_MILLIS_DISABLE = TimeUnit.DAYS.toMillis(30); // Disable
    public static final boolean KEEPALIVE_WITHOUT_CALLS_DISABLE = Boolean.FALSE;
    // <a href="https://tools.ietf.org/html/rfc7540#section-6.5.2">
    public static final int DEFAULT_MAX_HEADER_LIST_SIZE = 8 * 1024;
    public static final int DEFAULT_MAX_MESSAGE_SIZE = 4 * 1024 * 1024;
    // <a href="https://tools.ietf.org/html/rfc7540#section-6.9.2">initial connection flow-control window size</a>
    public static final int DEFAULT_FLOW_CONTROL_WINDOW = 1 * 1024 * 1024; // 1MiB
    public static final int INITIAL_FLOW_CONTROL_WINDOW = 65535;
    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;
    public static final int DEFAULT_WRITE_BUFFER_HIGH_WATER_MARK = 32 * 1024 * 1024;
    public static final int DEFAULT_WRITE_BUFFER_LOW_WATER_MARK = 16 * 1024 * 1024;
    public static final String DEFAULT_CHANNEL_TYPE = ChannelTypeEnum.AUTO.name();

    public static final int DEFAULT_MAX_TRACE_EVENT = 0;
    public static final int DEFAULT_LIMIT_COUNT = 100;
    public static final int DEFAULT_LIMIT_TIME = 60 * 1000;

    @Value("${keepalive.time.millis}")
    private long keepAliveTime = DEFAULT_KEEPALIVE_TIME;
    @Value("${keepalive.timeout.millis}")
    private long keepAliveTimeout = DEFAULT_KEEPALIVE_TIMEOUT;
    // KeepAliveManager.keepAliveDuringTransportIdle
    private boolean keepAliveWithoutCalls = KEEPALIVE_WITHOUT_CALLS_DISABLE;
    private long idleTimeoutMillis = IDLE_TIMEOUT_MILLIS_DISABLE;

    private int maxHeaderListSize = DEFAULT_MAX_HEADER_LIST_SIZE;
    private int maxInboundMessageSize = DEFAULT_MAX_MESSAGE_SIZE;
    private int flowControlWindow = DEFAULT_FLOW_CONTROL_WINDOW;

    // ChannelOption
    @Value("${connect.timeout.millis}")
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    private int writeBufferHighWaterMark = DEFAULT_WRITE_BUFFER_HIGH_WATER_MARK;
    private int writeBufferLowWaterMark = DEFAULT_WRITE_BUFFER_LOW_WATER_MARK;

    private ChannelTypeEnum channelTypeEnum = ChannelTypeEnum.AUTO;
    @Value("${maxtraceevent}")
    private int maxTraceEvent;
    @Value("${limitcount}")
    private int limitCount;
    @Value("${limittime}")
    private long limitTime;

    public ClientOption() {
    }

    public ClientOption(long keepAliveTime, long keepAliveTimeout, int maxHeaderListSize, int maxInboundMessageSize,
                         int flowControlWindow, int connectTimeout, int writeBufferHighWaterMark, int writeBufferLowWaterMark,
                        ChannelTypeEnum channelTypeEnum, int maxTraceEvent, int limitCount, long limitTime) {
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeout = keepAliveTimeout;
        this.flowControlWindow = flowControlWindow;
        this.maxHeaderListSize = maxHeaderListSize;
        this.maxInboundMessageSize = maxInboundMessageSize;
        this.connectTimeout = connectTimeout;
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;

        this.channelTypeEnum = Assert.requireNonNull(channelTypeEnum, "channelTypeEnum");
        this.maxTraceEvent = maxTraceEvent;

        this.limitCount = limitCount;
        this.limitTime = limitTime;
    }

    public int getFlowControlWindow() {
        return flowControlWindow;
    }

    public int getMaxHeaderListSize() {
        return maxHeaderListSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public long getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public boolean isKeepAliveWithoutCalls() {
        return keepAliveWithoutCalls;
    }

    public long getIdleTimeoutMillis() {
        return idleTimeoutMillis;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getWriteBufferHighWaterMark() {
        return writeBufferHighWaterMark;
    }

    public int getWriteBufferLowWaterMark() {
        return writeBufferLowWaterMark;
    }

    public ChannelTypeEnum getChannelTypeEnum() {
        return channelTypeEnum;
    }

    @Value("${channel-type}")
    void setChannelType(String channelType) {
        this.channelTypeEnum = ChannelTypeEnum.valueOf(channelType);
    }

    public int getMaxTraceEvent() {
        return maxTraceEvent;
    }

    public int getLimitCount() {
        return limitCount;
    }

    public long getLimitTime() {
        return limitTime;
    }

    @Value("${headers.size.max}")
    void setMaxHeaderListSize(String maxHeaderListSize) {
        this.maxHeaderListSize = (int) ByteSizeUnit.getByteSize(maxHeaderListSize, DEFAULT_MAX_HEADER_LIST_SIZE);
    }

    @Value("${message.inbound.size.max}")
    void setMaxInboundMessageSize(String maxInboundMessageSize) {
        this.maxInboundMessageSize = (int) ByteSizeUnit.getByteSize(maxInboundMessageSize, DEFAULT_MAX_MESSAGE_SIZE);
    }

    @Value("${flow-control.window.size}")
    void setFlowControlWindow(String flowControlWindow) {
        this.flowControlWindow = (int) ByteSizeUnit.getByteSize(flowControlWindow, DEFAULT_FLOW_CONTROL_WINDOW);
    }
    @Value("${write.buffer.highwatermark}")
    void setWriteBufferHighWaterMark(String writeBufferHighWaterMark) {
        this.writeBufferHighWaterMark = (int) ByteSizeUnit.getByteSize(writeBufferHighWaterMark, DEFAULT_WRITE_BUFFER_HIGH_WATER_MARK);
    }
    @Value("${write.buffer.lowwatermark}")
    void setWriteBufferLowWaterMark(String writeBufferLowWaterMark) {
        this.writeBufferLowWaterMark = (int) ByteSizeUnit.getByteSize(writeBufferLowWaterMark, DEFAULT_WRITE_BUFFER_LOW_WATER_MARK);
    }

    @Override
    public String toString() {
        return "ClientOption{" +
                "keepAliveTime=" + keepAliveTime +
                ", keepAliveTimeout=" + keepAliveTimeout +
                ", keepAliveWithoutCalls=" + keepAliveWithoutCalls +
                ", idleTimeoutMillis=" + idleTimeoutMillis +
                ", maxHeaderListSize=" + maxHeaderListSize +
                ", maxInboundMessageSize=" + maxInboundMessageSize +
                ", flowControlWindow=" + flowControlWindow +
                ", connectTimeout=" + connectTimeout +
                ", writeBufferHighWaterMark=" + writeBufferHighWaterMark +
                ", writeBufferLowWaterMark=" + writeBufferLowWaterMark +
                ", channelTypeEnum=" + channelTypeEnum +
                ", maxTraceEvent=" + maxTraceEvent +
                ", limitCount=" + limitCount +
                ", limitTime=" + limitTime +
                '}';
    }
}