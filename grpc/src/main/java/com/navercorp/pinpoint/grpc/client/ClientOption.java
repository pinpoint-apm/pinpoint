/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.client;

import java.util.concurrent.TimeUnit;

/**
 * @author jaehong.kim
 */
public class ClientOption {
    public static final long DEFAULT_KEEPALIVE_TIME = TimeUnit.MINUTES.toMillis(5);
    public static final long DEFAULT_KEEPALIVE_TIMEOUT = TimeUnit.MINUTES.toMillis(30);
    public static final long IDLE_TIMEOUT_MILLIS_DISABLE = -1;
    public static final boolean DEFAULT_KEEPALIVE_WITHOUT_CALLS = Boolean.FALSE;
    public static final int DEFAULT_MAX_HEADER_LIST_SIZE = 8192;
    public static final int DEFAULT_MAX_MESSAGE_SIZE = 4 * 1024 * 1024;
    public static final int DEFAULT_FLOW_CONTROL_WINDOW = 1048576; // 1MiB

    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;
    public static final int DEFAULT_WRITE_BUFFER_HIGH_WATER_MARK = 32 * 1024;
    public static final int DEFAULT_WRITE_BUFFER_LOW_WATER_MARK = 16 * 1024;

    private final long keepAliveTime;
    private final long keepAliveTimeout;
    // KeepAliveManager.keepAliveDuringTransportIdle
    private final boolean keepAliveWithoutCalls;
    private final long idleTimeoutMillis;
    private final int maxHeaderListSize;
    private final int maxInboundMessageSize;
    private final int flowControlWindow;

    // ChannelOption
    private final int connectTimeout;
    private final int writeBufferHighWaterMark;
    private final int writeBufferLowWaterMark;

    private ClientOption(long keepAliveTime, long keepAliveTimeout, boolean keepAliveWithoutCalls, long idleTimeoutMillis, int maxHeaderListSize, int maxInboundMessageSize, int flowControlWindow, int connectTimeout, int writeBufferHighWaterMark, int writeBufferLowWaterMark) {
        this.flowControlWindow = flowControlWindow;
        this.maxHeaderListSize = maxHeaderListSize;
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeout = keepAliveTimeout;
        this.keepAliveWithoutCalls = keepAliveWithoutCalls;
        this.idleTimeoutMillis = idleTimeoutMillis;
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
        private boolean keepAliveWithoutCalls = DEFAULT_KEEPALIVE_WITHOUT_CALLS;

        private long idleTimeoutMillis = IDLE_TIMEOUT_MILLIS_DISABLE;
        private int maxInboundMessageSize = DEFAULT_MAX_MESSAGE_SIZE;

        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private int writeBufferHighWaterMark = DEFAULT_WRITE_BUFFER_HIGH_WATER_MARK;
        private int writeBufferLowWaterMark = DEFAULT_WRITE_BUFFER_LOW_WATER_MARK;

        public ClientOption build() {
            final ClientOption clientOption = new ClientOption(keepAliveTime, keepAliveTimeout, keepAliveWithoutCalls, idleTimeoutMillis, maxHeaderListSize, maxInboundMessageSize, flowControlWindow, connectTimeout, writeBufferHighWaterMark, writeBufferLowWaterMark);
            return clientOption;
        }

        public void setFlowControlWindow(int flowControlWindow) {
            this.flowControlWindow = flowControlWindow;
        }

        public void setMaxHeaderListSize(int maxHeaderListSize) {
            this.maxHeaderListSize = maxHeaderListSize;
        }

        public void setKeepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public void setKeepAliveTimeout(long keepAliveTimeout) {
            this.keepAliveTimeout = keepAliveTimeout;
        }

        public void setKeepAliveWithoutCalls(boolean keepAliveWithoutCalls) {
            this.keepAliveWithoutCalls = keepAliveWithoutCalls;
        }

        public void setIdleTimeoutMillis(long idleTimeoutMillis) {
            this.idleTimeoutMillis = idleTimeoutMillis;
        }

        public void setMaxInboundMessageSize(int maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
        }

        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
            this.writeBufferHighWaterMark = writeBufferHighWaterMark;
        }

        public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
            this.writeBufferLowWaterMark = writeBufferLowWaterMark;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Builder{");
            sb.append("flowControlWindow=").append(flowControlWindow);
            sb.append(", maxHeaderListSize=").append(maxHeaderListSize);
            sb.append(", keepAliveTime=").append(keepAliveTime);
            sb.append(", keepAliveTimeout=").append(keepAliveTimeout);
            sb.append(", keepAliveWithoutCalls=").append(keepAliveWithoutCalls);
            sb.append(", idleTimeoutMillis=").append(idleTimeoutMillis);
            sb.append(", maxInboundMessageSize=").append(maxInboundMessageSize);
            sb.append(", connectTimeout=").append(connectTimeout);
            sb.append(", writeBufferHighWaterMark=").append(writeBufferHighWaterMark);
            sb.append(", writeBufferLowWaterMark=").append(writeBufferLowWaterMark);
            sb.append('}');
            return sb.toString();
        }
    }
}