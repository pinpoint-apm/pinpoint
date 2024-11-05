package com.navercorp.pinpoint.redis.timeseries.connection;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.protocol.AsyncCommand;
import io.lettuce.core.protocol.RedisCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AsyncDispatcher<K, V> implements Dispatcher<K, V> {

    private final StatefulConnection<K, V> connection;

    public AsyncDispatcher(StatefulConnection<K, V> connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    @Override
    public <T> RedisFuture<T> dispatch(RedisCommand<K, V, T> command) {
        AsyncCommand<K, V, T> asyncCommand = wrapAsync(command);

        RedisCommand<K, V, T> result = connection.dispatch(asyncCommand);

        return (AsyncCommand<K, V, T>) result;
    }

    private <T> AsyncCommand<K, V, T> wrapAsync(RedisCommand<K, V, T> command) {
        return new AsyncCommand<>(command);
    }

    @Override
    public <T> Collection<RedisFuture<T>> dispatch(Collection<RedisCommand<K, V, T>> command) {
        List<AsyncCommand<K, V, T>> async = wrapAsyncCommands(command);

        Collection<RedisCommand<K, V, ?>> result = connection.dispatch(async);

//        List<? extends RedisFuture<?>> list1 = result.stream().map(c -> (RedisFuture<?>) c).toList();
        return (Collection<RedisFuture<T>>) (Collection<?>) result;
    }

    private <T> List<AsyncCommand<K, V, T>> wrapAsyncCommands(Collection<RedisCommand<K, V, T>> command) {
        List<AsyncCommand<K, V, T>> result = new ArrayList<>(command.size());
        for (RedisCommand<K, V, T> redisCommand : command) {
            result.add(wrapAsync(redisCommand));
        }
        return result;
    }
}
