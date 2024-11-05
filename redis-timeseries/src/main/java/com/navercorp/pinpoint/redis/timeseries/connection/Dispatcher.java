package com.navercorp.pinpoint.redis.timeseries.connection;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.protocol.RedisCommand;

import java.util.Collection;

public interface Dispatcher<K, V> {

    <T> RedisFuture<T> dispatch(RedisCommand<K, V, T> command);

    <T> Collection<RedisFuture<T>> dispatch(Collection<RedisCommand<K, V, T>> commands);

//    default Collection<RedisFuture<?>> dispatch(Collection<RedisCommand<K, V, ?>> commands) {
//        return dispatch(commands);
//    }
}
