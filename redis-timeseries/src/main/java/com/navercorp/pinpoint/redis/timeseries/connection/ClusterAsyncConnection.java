package com.navercorp.pinpoint.redis.timeseries.connection;

import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisClusterAsyncCommands;

import java.util.Objects;

public class ClusterAsyncConnection<K, V> implements AsyncConnection<K, V> {
    private final StatefulRedisClusterConnection<K, V> connection;
    private final AsyncDispatcher<K, V> dispatcher;
    private final SyncDispatcher<K, V> sync;

    public ClusterAsyncConnection(StatefulRedisClusterConnection<K, V> connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
        this.dispatcher = new AsyncDispatcher<>(connection);
        this.sync = new SyncDispatcher<>(connection.sync());
    }

    @Override
    public Dispatcher<K, V> dispatcher() {
        return dispatcher;
    }

    public SyncDispatcher<K, V> sync() {
        return sync;
    }

    RedisClusterAsyncCommands<K, V> commands() {
        // pipelining
        // connection.setAutoFlushCommands(false);
        return connection.async();
    }

    @Override
    public void close() {
        connection.close();
    }
}
