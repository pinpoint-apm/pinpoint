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

package com.navercorp.pinpoint.grpc.client;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.concurrent.TimeUnit;

/**
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

    private final long keepAliveTime;
    private final long keepAliveTimeout;
    // KeepAliveManager.keepAliveDuringTransportIdle
    private final boolean keepAliveWithoutCalls = KEEPALIVE_WITHOUT_CALLS_DISABLE;
    private final long idleTimeoutMillis = IDLE_TIMEOUT_MILLIS_DISABLE;
    private final int maxHeaderListSize;
    private final int maxInboundMessageSize;
    private final int flowControlWindow;

    // ChannelOption
    private final int connectTimeout;
    private final int writeBufferHighWaterMark;
    private final int writeBufferLowWaterMark;

    private ClientOption(long keepAliveTime, long keepAliveTimeout, int maxHeaderListSize, int maxInboundMessageSize, int flowControlWindow, int connectTimeout, int writeBufferHighWaterMark, int writeBufferLowWaterMark) {
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeout = keepAliveTimeout;
        this.flowControlWindow = flowControlWindow;
        this.maxHeaderListSize = maxHeaderListSize;
        this.maxInboundMessageSize = maxInboundMessageSize;
        this.connectTimeout = connectTimeout;
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClientOption{");
        sb.append("keepAliveTime=").append(keepAliveTime);
        sb.append(", keepAliveTimeout=").append(keepAliveTimeout);
        sb.append(", keepAliveWithoutCalls=").append(keepAliveWithoutCalls);
        sb.append(", idleTimeoutMillis=").append(idleTimeoutMillis);
        sb.append(", maxHeaderListSize=").append(maxHeaderListSize);
        sb.append(", maxInboundMessageSize=").append(maxInboundMessageSize);
        sb.append(", flowControlWindow=").append(flowControlWindow);
        sb.append(", connectTimeout=").append(connectTimeout);
        sb.append(", writeBufferHighWaterMark=").append(writeBufferHighWaterMark);
        sb.append(", writeBufferLowWaterMark=").append(writeBufferLowWaterMark);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private int flowControlWindow = DEFAULT_FLOW_CONTROL_WINDOW;
        private int maxHeaderListSize = DEFAULT_MAX_HEADER_LIST_SIZE;
        private long keepAliveTime = DEFAULT_KEEPALIVE_TIME;
        private long keepAliveTimeout = DEFAULT_KEEPALIVE_TIMEOUT;

        private int maxInboundMessageSize = DEFAULT_MAX_MESSAGE_SIZE;

        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private int writeBufferHighWaterMark = DEFAULT_WRITE_BUFFER_HIGH_WATER_MARK;
        private int writeBufferLowWaterMark = DEFAULT_WRITE_BUFFER_LOW_WATER_MARK;

        public ClientOption build() {
            final ClientOption clientOption = new ClientOption(keepAliveTime, keepAliveTimeout, maxHeaderListSize, maxInboundMessageSize, flowControlWindow, connectTimeout, writeBufferHighWaterMark, writeBufferLowWaterMark);
            return clientOption;
        }

        public void setFlowControlWindow(int flowControlWindow) {
            if (!(flowControlWindow >= INITIAL_FLOW_CONTROL_WINDOW)) {
                throw new IllegalArgumentException("flowControlWindow expected >= " + INITIAL_FLOW_CONTROL_WINDOW);
            }
            this.flowControlWindow = flowControlWindow;
        }

        public void setMaxHeaderListSize(int maxHeaderListSize) {
            Assert.isTrue(maxHeaderListSize > 0, "maxHeaderListSize must be positive");
            this.maxHeaderListSize = maxHeaderListSize;
        }

        public void setKeepAliveTime(long keepAliveTime) {
            Assert.isTrue(keepAliveTime > 0, "keepAliveTime must be positive");
            this.keepAliveTime = keepAliveTime;
        }

        public void setKeepAliveTimeout(long keepAliveTimeout) {
            Assert.isTrue(keepAliveTimeout > 0, "keepAliveTimeout must be positive");
            this.keepAliveTimeout = keepAliveTimeout;
        }

        public void setMaxInboundMessageSize(int maxInboundMessageSize) {
            Assert.isTrue(maxInboundMessageSize > 0, "maxInboundMessageSize must be positive");
            this.maxInboundMessageSize = maxInboundMessageSize;
        }

        public void setConnectTimeout(int connectTimeout) {
            Assert.isTrue(connectTimeout > 0, "connectTimeout must be positive");
            this.connectTimeout = connectTimeout;
        }

        public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
            Assert.isTrue(writeBufferHighWaterMark > 0, "writeBufferHighWaterMark must be positive");
            this.writeBufferHighWaterMark = writeBufferHighWaterMark;
        }

        public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
            Assert.isTrue(writeBufferLowWaterMark > 0, "writeBufferLowWaterMark must be positive");
            this.writeBufferLowWaterMark = writeBufferLowWaterMark;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Builder{");
            sb.append("flowControlWindow=").append(flowControlWindow);
            sb.append(", maxHeaderListSize=").append(maxHeaderListSize);
            sb.append(", keepAliveTime=").append(keepAliveTime);
            sb.append(", keepAliveTimeout=").append(keepAliveTimeout);
            sb.append(", maxInboundMessageSize=").append(maxInboundMessageSize);
            sb.append(", connectTimeout=").append(connectTimeout);
            sb.append(", writeBufferHighWaterMark=").append(writeBufferHighWaterMark);
            sb.append(", writeBufferLowWaterMark=").append(writeBufferLowWaterMark);
            sb.append('}');
            return sb.toString();
        }
    }
}