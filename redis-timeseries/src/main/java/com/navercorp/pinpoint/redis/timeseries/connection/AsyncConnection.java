package com.navercorp.pinpoint.redis.timeseries.connection;

public interface AsyncConnection<K, V> extends AutoCloseable {
    Dispatcher<K, V> dispatcher();

    SyncDispatcher<K, V> sync();

    @Override
    void close();
}
