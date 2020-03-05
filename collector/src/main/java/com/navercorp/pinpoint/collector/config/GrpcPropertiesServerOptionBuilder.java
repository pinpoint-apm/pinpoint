package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.ConfigurationUtils;
import com.navercorp.pinpoint.common.util.ByteSizeUnit;
import com.navercorp.pinpoint.grpc.server.ServerOption;

import java.util.Properties;

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

    public static ServerOption.Builder newBuilder(final Properties properties, final String transportName) {
        final ServerOption.Builder builder = new ServerOption.Builder();

        builder.setKeepAliveTime(ConfigurationUtils.readLong(properties, transportName + KEEP_ALIVE_TIME, ServerOption.DEFAULT_KEEPALIVE_TIME));
        builder.setKeepAliveTimeout(ConfigurationUtils.readLong(properties, transportName + KEEP_ALIVE_TIMEOUT, ServerOption.DEFAULT_KEEPALIVE_TIMEOUT));
        builder.setPermitKeepAliveTime(ConfigurationUtils.readLong(properties, transportName + PERMIT_KEEPALIVE_TIMEOUT, ServerOption.DEFAULT_PERMIT_KEEPALIVE_TIME));
        builder.setMaxConnectionIdle(ConfigurationUtils.readLong(properties, transportName + MAX_CONNECTION_IDLE, ServerOption.DEFAULT_MAX_CONNECTION_IDLE));
        builder.setHandshakeTimeout(ConfigurationUtils.readLong(properties, transportName + HANDSHAKE_TIMEOUT, ServerOption.DEFAULT_HANDSHAKE_TIMEOUT));
        builder.setMaxConcurrentCallsPerConnection(ConfigurationUtils.readInt(properties, transportName + MAX_CONCURRENT_CALLS_PER_CONNECTION, ServerOption.DEFAULT_MAX_CONCURRENT_CALLS_PER_CONNECTION));

        builder.setMaxInboundMessageSize(readByteSize(properties, transportName + MAX_INBOUND_MESSAGE_SIZE, ServerOption.DEFAULT_MAX_INBOUND_MESSAGE_SIZE));
        builder.setFlowControlWindow(readByteSize(properties, transportName + FLOW_CONTROL_WINDOW, ServerOption.DEFAULT_FLOW_CONTROL_WINDOW));
        builder.setMaxHeaderListSize(readByteSize(properties, transportName + MAX_HEADER_LIST_SIZE, ServerOption.DEFAULT_MAX_HEADER_LIST_SIZE));
        builder.setReceiveBufferSize(readByteSize(properties, transportName + RECEIVE_BUFFER_SIZE, ServerOption.DEFAULT_RECEIVE_BUFFER_SIZE));

        return builder;
    }

    private static int readByteSize(final Properties properties, final String propertyName, final int defaultValue) {
        return (int) ByteSizeUnit.getByteSize(ConfigurationUtils.readString(properties, propertyName, ""), defaultValue);
    }
}