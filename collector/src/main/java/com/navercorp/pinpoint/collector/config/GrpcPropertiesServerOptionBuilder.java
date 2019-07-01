package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.grpc.server.ServerOption;

import java.util.Properties;

import static com.navercorp.pinpoint.collector.config.CollectorConfiguration.*;

public class GrpcPropertiesServerOptionBuilder {
    private static final String KEEP_ALIVE_TIME = ".keepalive.time.millis";
    private static final String KEEP_ALIVE_TIMEOUT = ".keepalive.timeout.millis";
    private static final String PERMIT_KEEPALIVE_TIMEOUT = ".permit.keepalive.timeout.millis";
    private static final String MAX_CONNECTION_IDLE = ".connection.idle.timeout.millis";
    private static final String MAX_CONCURRENT_CALLS_PER_CONNECTION = ".concurrent-calls.per-connection.max";
    private static final String FLOW_CONTROL_WINDOW = ".flow-control.window";
    private static final String MAX_HEADER_LIST_SIZE = ".header.list.size.max";
    private static final String HANDSHAKE_TIMEOUT = ".handshake.timeout.millis";
    private static final String MAX_INBOUND_MESSAGE_SIZE = ".inbound.message.size.max";
    private static final String RECEIVE_BUFFER_SIZE = ".receive.buffer.size";
    private static final String BACKLOG_QUEUE_SIZE = ".backlog.queue.size";
    private static final String CONNECT_TIMEOUT = ".connect.timeout.millis";
    private static final String WRITE_BUFFER_HIGH_WATER_MARK = ".write.buffer.high-water-mark";
    private static final String WRITE_BUFFER_LOW_WATER_MARK = ".write.buffer.low-water-mark";

    public static ServerOption.Builder newBuilder(final Properties properties, final String transportName) {
        final ServerOption.Builder builder = new ServerOption.Builder();

        builder.setKeepAliveTime(readLong(properties, transportName + KEEP_ALIVE_TIME, ServerOption.DEFAULT_KEEPALIVE_TIME));
        builder.setKeepAliveTimeout(readLong(properties, transportName + KEEP_ALIVE_TIMEOUT, ServerOption.DEFAULT_KEEPALIVE_TIMEOUT));
        builder.setPermitKeepAliveTimeout(readLong(properties, transportName + PERMIT_KEEPALIVE_TIMEOUT, ServerOption.DEFAULT_PERMIT_KEEPALIVE_TIMEOUT));

        builder.setMaxConnectionIdle(readLong(properties, transportName + MAX_CONNECTION_IDLE, ServerOption.DEFAULT_MAX_CONNECTION_IDLE));

        builder.setMaxConcurrentCallsPerConnection(readInt(properties, transportName + MAX_CONCURRENT_CALLS_PER_CONNECTION, ServerOption.DEFAULT_MAX_CONCURRENT_CALLS_PER_CONNECTION));
        builder.setMaxInboundMessageSize(readInt(properties, transportName + MAX_INBOUND_MESSAGE_SIZE, ServerOption.DEFAULT_MAX_INBOUND_MESSAGE_SIZE));
        builder.setFlowControlWindow(readInt(properties, transportName + FLOW_CONTROL_WINDOW, ServerOption.DEFAULT_FLOW_CONTROL_WINDOW));
        builder.setMaxHeaderListSize(readInt(properties, transportName + MAX_HEADER_LIST_SIZE, ServerOption.DEFAULT_MAX_HEADER_LIST_SIZE));

        builder.setHandshakeTimeout(readLong(properties, transportName + HANDSHAKE_TIMEOUT, ServerOption.DEFAULT_HANDSHAKE_TIMEOUT));

        builder.setReceiveBufferSize(readInt(properties, transportName + RECEIVE_BUFFER_SIZE, ServerOption.DEFAULT_RECEIVE_BUFFER_SIZE));
        builder.setBacklogQueueSize(readInt(properties, transportName + BACKLOG_QUEUE_SIZE, ServerOption.DEFAULT_BACKLOG_QUEUE_SIZE));
        builder.setConnectTimeout(readInt(properties, transportName + CONNECT_TIMEOUT, ServerOption.DEFAULT_CONNECT_TIMEOUT));
        builder.setWriteBufferHighWaterMark(readInt(properties, transportName + WRITE_BUFFER_HIGH_WATER_MARK, ServerOption.DEFAULT_WRITE_BUFFER_HIGH_WATER_MARK));
        builder.setWriteBufferLowWaterMark(readInt(properties, transportName + WRITE_BUFFER_LOW_WATER_MARK, ServerOption.DEFAULT_WRITE_BUFFER_LOW_WATER_MARK));

        return builder;
    }
}