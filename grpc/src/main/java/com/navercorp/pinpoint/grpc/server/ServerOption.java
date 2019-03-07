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

package com.navercorp.pinpoint.grpc.server;

import java.util.concurrent.TimeUnit;

public class ServerOption {
    private static final int DEFAULT_FLOW_CONTROL_WINDOW = 1048576; // 1MiB
    private static final long DEFAULT_KEEPALIVE_TIME = TimeUnit.MINUTES.toMillis(5);
    private static final long DEFAULT_KEEPALIVE_TIMEOUT = TimeUnit.MINUTES.toMillis(30);
    private static final long DEFAULT_PERMIT_KEEPALIVE_TIMEOUT = TimeUnit.MINUTES.toMillis(60);
    private static final boolean DEFAULT_PERMIT_KEEPALIVE_WITHOUT_CALLS = Boolean.FALSE;

    private static final long DEFAULT_MAX_CONNECTION_IDLE = Long.MAX_VALUE; // Disabled
    private static final long DEFAULT_MAX_CONNECTION_AGE = Long.MAX_VALUE; // Disabled
    private static final long DEFAULT_MAX_CONNECTION_AGE_GRACE = Long.MAX_VALUE; // Infinite
    private static final int DEFAULT_MAX_CONCURRENT_CALLS_PER_CONNECTION = Integer.MAX_VALUE; // Infinite

    private static final int DEFAULT_MAX_INBOUND_MESSAGE_SIZE = 4 * 1024 * 1024;
    private static final int DEFAULT_MAX_HEADER_LIST_SIZE = 8192;

    private static final long DEFAULT_HANDSHAKE_TIMEOUT = TimeUnit.SECONDS.toMillis(120);

    // Sets a custom keepalive time, the delay time for sending next keepalive ping.
    private final long keepAliveTime;
    // Sets a custom keepalive timeout, the timeout for keepalive ping requests.
    private final long keepAliveTimeout;
    // Specify the most aggressive keep-alive time clients are permitted to configure.
    private final long permitKeepAliveTimeout;
    // Sets whether to allow clients to send keep-alive HTTP/2 PINGs even if there are no outstanding RPCs on the connection. Defaults to {@code false}.
    private final boolean permitKeepAliveWithoutCalls;

    // Sets a custom max connection idle time, connection being idle for longer than which will be gracefully terminated.
    private final long maxConnectionIdle;
    // Sets a custom max connection age, connection lasting longer than which will be gracefully terminated.
    private final long maxConnectionAge;
    // Sets a custom grace time for the graceful connection termination. Once the max connection age is reached, RPCs have the grace time to complete.
    private final long maxConnectionAgeGrace;
    // The maximum number of concurrent calls permitted for each incoming connection. Defaults to no limit.
    private final int maxConcurrentCallsPerConnection;

    // Sets the maximum message size allowed to be received on the server.
    private final int maxInboundMessageSize;
    // Sets the maximum size of metadata allowed to be received.
    private final int maxHeaderListSize;

    private final long handshakeTimeout;
    // Sets the HTTP/2 flow control window.
    private final int flowControlWindow;

    ServerOption(long keepAliveTime, long keepAliveTimeout, long permitKeepAliveTimeout, boolean permitKeepAliveWithoutCalls, long maxConnectionIdle, long maxConnectionAge, long maxConnectionAgeGrace, int maxConcurrentCallsPerConnection, int maxInboundMessageSize, int maxHeaderListSize, long handshakeTimeout, int flowControlWindow) {
        this.keepAliveTime = keepAliveTime;
        this.keepAliveTimeout = keepAliveTimeout;
        this.permitKeepAliveTimeout = permitKeepAliveTimeout;
        this.permitKeepAliveWithoutCalls = permitKeepAliveWithoutCalls;
        this.maxConnectionIdle = maxConnectionIdle;
        this.maxConnectionAge = maxConnectionAge;
        this.maxConnectionAgeGrace = maxConnectionAgeGrace;
        this.maxConcurrentCallsPerConnection = maxConcurrentCallsPerConnection;
        this.maxInboundMessageSize = maxInboundMessageSize;
        this.maxHeaderListSize = maxHeaderListSize;
        this.handshakeTimeout = handshakeTimeout;
        this.flowControlWindow = flowControlWindow;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public long getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public long getPermitKeepAliveTimeout() {
        return permitKeepAliveTimeout;
    }

    public boolean isPermitKeepAliveWithoutCalls() {
        return permitKeepAliveWithoutCalls;
    }

    public long getMaxConnectionIdle() {
        return maxConnectionIdle;
    }

    public long getMaxConnectionAge() {
        return maxConnectionAge;
    }

    public long getMaxConnectionAgeGrace() {
        return maxConnectionAgeGrace;
    }

    public int getMaxConcurrentCallsPerConnection() {
        return maxConcurrentCallsPerConnection;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public int getMaxHeaderListSize() {
        return maxHeaderListSize;
    }

    public long getHandshakeTimeout() {
        return handshakeTimeout;
    }

    public int getFlowControlWindow() {
        return flowControlWindow;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ServerOption{");
        sb.append("keepAliveTime=").append(keepAliveTime);
        sb.append(", keepAliveTimeout=").append(keepAliveTimeout);
        sb.append(", permitKeepAliveTimeout=").append(permitKeepAliveTimeout);
        sb.append(", permitKeepAliveWithoutCalls=").append(permitKeepAliveWithoutCalls);
        sb.append(", maxConnectionIdle=").append(maxConnectionIdle);
        sb.append(", maxConnectionAge=").append(maxConnectionAge);
        sb.append(", maxConnectionAgeGrace=").append(maxConnectionAgeGrace);
        sb.append(", maxConcurrentCallsPerConnection=").append(maxConcurrentCallsPerConnection);
        sb.append(", maxInboundMessageSize=").append(maxInboundMessageSize);
        sb.append(", maxHeaderListSize=").append(maxHeaderListSize);
        sb.append(", handshakeTimeout=").append(handshakeTimeout);
        sb.append(", flowControlWindow=").append(flowControlWindow);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        // Sets a custom keepalive time, the delay time for sending next keepalive ping.
        private long keepAliveTime = DEFAULT_KEEPALIVE_TIME;
        // Sets a custom keepalive timeout, the timeout for keepalive ping requests.
        private long keepAliveTimeout = DEFAULT_KEEPALIVE_TIMEOUT;
        // Specify the most aggressive keep-alive time clients are permitted to configure.
        private long permitKeepAliveTimeout = DEFAULT_PERMIT_KEEPALIVE_TIMEOUT;
        // Sets whether to allow clients to send keep-alive HTTP/2 PINGs even if there are no outstanding RPCs on the connection. Defaults to {@code false}.
        private boolean permitKeepAliveWithoutCalls = DEFAULT_PERMIT_KEEPALIVE_WITHOUT_CALLS;

        // Sets a custom max connection idle time, connection being idle for longer than which will be gracefully terminated.
        private long maxConnectionIdle = DEFAULT_MAX_CONNECTION_IDLE;
        // Sets a custom max connection age, connection lasting longer than which will be gracefully terminated.
        private long maxConnectionAge = DEFAULT_MAX_CONNECTION_AGE;
        // Sets a custom grace time for the graceful connection termination. Once the max connection age is reached, RPCs have the grace time to complete.
        private long maxConnectionAgeGrace = DEFAULT_MAX_CONNECTION_AGE_GRACE;
        // The maximum number of concurrent calls permitted for each incoming connection. Defaults to no limit.
        private int maxConcurrentCallsPerConnection = DEFAULT_MAX_CONCURRENT_CALLS_PER_CONNECTION;

        // Sets the maximum message size allowed to be received on the server.
        private int maxInboundMessageSize = DEFAULT_MAX_INBOUND_MESSAGE_SIZE;
        // Sets the maximum size of metadata allowed to be received.
        private int maxHeaderListSize = DEFAULT_MAX_HEADER_LIST_SIZE;

        private long handshakeTimeout = DEFAULT_HANDSHAKE_TIMEOUT;
        // Sets the HTTP/2 flow control window.
        private int flowControlWindow = DEFAULT_FLOW_CONTROL_WINDOW;

        public ServerOption build() {
            final ServerOption serverOption = new ServerOption(keepAliveTime, keepAliveTimeout, permitKeepAliveTimeout, permitKeepAliveWithoutCalls, maxConnectionIdle, maxConnectionAge, maxConnectionAgeGrace, maxConcurrentCallsPerConnection, maxInboundMessageSize, maxHeaderListSize, handshakeTimeout, flowControlWindow);
            return serverOption;
        }

        public void setKeepAliveTime(long keepAliveTime) {
            this.keepAliveTime = keepAliveTime;
        }

        public void setKeepAliveTimeout(long keepAliveTimeout) {
            this.keepAliveTimeout = keepAliveTimeout;
        }

        public void setPermitKeepAliveTimeout(long permitKeepAliveTimeout) {
            this.permitKeepAliveTimeout = permitKeepAliveTimeout;
        }

        public void setPermitKeepAliveWithoutCalls(boolean permitKeepAliveWithoutCalls) {
            this.permitKeepAliveWithoutCalls = permitKeepAliveWithoutCalls;
        }

        public void setMaxConnectionIdle(long maxConnectionIdle) {
            this.maxConnectionIdle = maxConnectionIdle;
        }

        public void setMaxConnectionAge(long maxConnectionAge) {
            this.maxConnectionAge = maxConnectionAge;
        }

        public void setMaxConnectionAgeGrace(long maxConnectionAgeGrace) {
            this.maxConnectionAgeGrace = maxConnectionAgeGrace;
        }

        public void setMaxConcurrentCallsPerConnection(int maxConcurrentCallsPerConnection) {
            this.maxConcurrentCallsPerConnection = maxConcurrentCallsPerConnection;
        }

        public void setMaxInboundMessageSize(int maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
        }

        public void setMaxHeaderListSize(int maxHeaderListSize) {
            this.maxHeaderListSize = maxHeaderListSize;
        }

        public void setHandshakeTimeout(long handshakeTimeout) {
            this.handshakeTimeout = handshakeTimeout;
        }

        public void setFlowControlWindow(int flowControlWindow) {
            this.flowControlWindow = flowControlWindow;
        }
    }
}