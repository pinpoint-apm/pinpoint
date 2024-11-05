package com.navercorp.pinpoint.redis.timeseries.connection;

import io.lettuce.core.api.StatefulRedisConnection;

import java.util.Objects;

public class SimpleAsyncConnection<K, V> implements AsyncConnection<K, V> {
    private final StatefulRedisConnection<K, V> connection;
    private final AsyncDispatcher<K, V> dispatcher;
    private final SyncDispatcher<K, V> sync;

    public SimpleAsyncConnection(StatefulRedisConnection<K, V> connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.dispatcher = new AsyncDispatcher<>(connection);
        this.sync = new SyncDispatcher<>(connection.sync());
    }

    public Dispatcher<K, V> dispatcher() {
        return dispatcher;
    }

    public SyncDispatcher<K, V> sync() {
        // pipelining
        // connection.setAutoFlushCommands(false);
        return sync;
    }

    @Override
    public void close() {
        connection.close();
    }
}
