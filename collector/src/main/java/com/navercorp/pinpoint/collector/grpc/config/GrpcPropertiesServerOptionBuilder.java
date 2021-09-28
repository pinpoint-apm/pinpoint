/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.grpc.server.ServerOption;
import org.springframework.util.unit.DataSize;

import java.util.Objects;

/**
 * @author jaehong.kim
 * @author emeroad
 */
public class GrpcPropertiesServerOptionBuilder {
    private static final String KEEP_ALIVE_TIME = ".keepalive_time_millis";
    private static final String KEEP_ALIVE_TIMEOUT = ".keepalive_timeout_millis";
    private static final String PERMIT_KEEPALIVE_TIMEOUT = ".permit_keepalive_time_millis";

    private static final String MAX_CONNECTION_IDLE = ".connection_idle_timeout_millis";
    private static final String MAX_CONCURRENT_CALLS_PER_CONNECTION = ".concurrent-calls_per-connection_max";
    private static final String FLOW_CONTROL_WINDOW = ".flow-control_window_size_init";
    private static final String MAX_HEADER_LIST_SIZE = ".header_list_size_max";
    private static final String HANDSHAKE_TIMEOUT = ".handshake_timeout_millis";
    private static final String MAX_INBOUND_MESSAGE_SIZE = ".inbound_message_size_max";
    private static final String RECEIVE_BUFFER_SIZE = ".receive_buffer_size";
    private static final String CHANNEL_TYPE = ".channel-type";

    private final ServerOption.Builder builder = ServerOption.newBuilder();

    public ServerOption build() {
        return builder.build();
    }

    public void setKeepAliveTimeMillis(long keepAliveTime) {
        builder.setKeepAliveTime(keepAliveTime);
    }

    public void setKeepAliveTimeoutMillis(long keepAliveTimeout) {
        builder.setKeepAliveTimeout(keepAliveTimeout);
    }

    public void setPermitKeepAliveTimeMillis(long permitKeepAliveTime) {
        builder.setPermitKeepAliveTime(permitKeepAliveTime);
    }

    public void setConnectionIdleTimeoutMillis(long maxConnectionIdle) {
        builder.setMaxConnectionIdle(maxConnectionIdle);
    }

    public void setConcurrentCallsPerConnectionMax(int maxConcurrentCallsPerConnection) {
        builder.setMaxConcurrentCallsPerConnection(maxConcurrentCallsPerConnection);
    }

    public void setInboundMessageSizeMax(DataSize maxInboundMessageSize) {
        builder.setMaxInboundMessageSize((int) maxInboundMessageSize.toBytes());
    }

    public void setHeaderListSizeMax(DataSize maxHeaderListSize) {
        builder.setMaxHeaderListSize((int) maxHeaderListSize.toBytes());
    }

    public void setHandshakeTimeoutMillis(long handshakeTimeout) {
        builder.setHandshakeTimeout(handshakeTimeout);
    }

    public void setFlowControlWindowSizeInit(DataSize flowControlWindowStr) {
        builder.setFlowControlWindow((int) flowControlWindowStr.toBytes());
    }


    public  void setReceiveBufferSize(DataSize receiveBufferSize) {
        builder.setReceiveBufferSize((int) receiveBufferSize.toBytes());
    }

    public void setChannelType(String channelTypeEnum) {
        Objects.requireNonNull(channelTypeEnum, "channelTypeEnum");
        builder.setChannelTypeEnum(channelTypeEnum);
    }

}