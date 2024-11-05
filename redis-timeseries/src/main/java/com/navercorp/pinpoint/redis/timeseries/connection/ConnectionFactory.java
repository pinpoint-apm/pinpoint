package com.navercorp.pinpoint.redis.timeseries.connection;

public interface ConnectionFactory<K, V> {
    AsyncConnection<K, V> getConnection();
}
