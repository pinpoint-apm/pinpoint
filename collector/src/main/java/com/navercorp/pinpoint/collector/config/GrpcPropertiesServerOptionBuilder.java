package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.EnvironmentHelper;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import org.springframework.core.env.Environment;

/**
 * @author jaehong.kim
 */
public class GrpcPropertiesServerOptionBuilder {
    private static final String KEEP_ALIVE_TIME = ".keepalive.time.millis";
    private static final String KEEP_ALIVE_TIMEOUT = ".keepalive.timeout.millis";
    private static final String PERMIT_KEEPALIVE_TIMEOUT = ".permit.keepalive.time.millis";
    private static final String MAX_CONNECTION_IDLE = ".connection.idle.timeout.millis";
    private static final String MAX_CONCURRENT_CALLS_PER_CONNECTION = ".concurrent-calls.per-connection.max";
    private static final String FLOW_CONTROL_WINDOW = ".flow-control.window.size.init";
    private static final String MAX_HEADER_LIST_SIZE = ".header.list.size.max";
    private static final String HANDSHAKE_TIMEOUT = ".handshake.timeout.millis";
    private static final String MAX_INBOUND_MESSAGE_SIZE = ".inbound.message.size.max";
    private static final String RECEIVE_BUFFER_SIZE = ".receive.buffer.size";
    private static final String CHANNEL_TYPE = ".channel-type";

    public static ServerOption.Builder newBuilder(final Environment environment, final String transportName) {
        EnvironmentHelper helper = new EnvironmentHelper(environment, transportName);

        final ServerOption.Builder builder = new ServerOption.Builder();

        builder.setKeepAliveTime(helper.getLong(KEEP_ALIVE_TIME, ServerOption.DEFAULT_KEEPALIVE_TIME));
        builder.setKeepAliveTimeout(helper.getLong(KEEP_ALIVE_TIMEOUT, ServerOption.DEFAULT_KEEPALIVE_TIMEOUT));
        builder.setPermitKeepAliveTime(helper.getLong(PERMIT_KEEPALIVE_TIMEOUT, ServerOption.DEFAULT_PERMIT_KEEPALIVE_TIME));
        builder.setMaxConnectionIdle(helper.getLong(MAX_CONNECTION_IDLE, ServerOption.DEFAULT_MAX_CONNECTION_IDLE));
        builder.setHandshakeTimeout(helper.getLong(HANDSHAKE_TIMEOUT, ServerOption.DEFAULT_HANDSHAKE_TIMEOUT));
        builder.setMaxConcurrentCallsPerConnection(helper.getInt(MAX_CONCURRENT_CALLS_PER_CONNECTION, ServerOption.DEFAULT_MAX_CONCURRENT_CALLS_PER_CONNECTION));

        builder.setMaxInboundMessageSize(helper.getByteSize( MAX_INBOUND_MESSAGE_SIZE, ServerOption.DEFAULT_MAX_INBOUND_MESSAGE_SIZE));
        builder.setFlowControlWindow(helper.getByteSize(FLOW_CONTROL_WINDOW, ServerOption.DEFAULT_FLOW_CONTROL_WINDOW));
        builder.setMaxHeaderListSize(helper.getByteSize(MAX_HEADER_LIST_SIZE, ServerOption.DEFAULT_MAX_HEADER_LIST_SIZE));
        builder.setReceiveBufferSize(helper.getByteSize(RECEIVE_BUFFER_SIZE, ServerOption.DEFAULT_RECEIVE_BUFFER_SIZE));
        builder.setChannelTypeEnum(helper.getString(CHANNEL_TYPE, ServerOption.DEFAULT_CHANNEL_TYPE));

        return builder;
    }

}