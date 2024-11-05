package com.navercorp.pinpoint.redis.timeseries;

import com.google.common.base.Preconditions;
import com.navercorp.pinpoint.redis.timeseries.connection.AsyncConnection;
import com.navercorp.pinpoint.redis.timeseries.connection.Dispatcher;
import com.navercorp.pinpoint.redis.timeseries.model.TimestampValuePair;
import io.lettuce.core.CompositeArgument;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.CommandArgs;

import java.util.List;
import java.util.Objects;

public class RedisTimeseriesAsyncCommandsImpl implements RedisTimeseriesAsyncCommands {

    private final RedisCodec<String, String> commandCodec = StringCodec.ASCII;

    private final RedisCodec<String, String> outputCodec = StringCodec.UTF8;

    private final AsyncConnection<String, String> connection;

    private final TimeseriesCommandBuilder<String, String> builder = new TimeseriesCommandBuilder<>(commandCodec);

    public RedisTimeseriesAsyncCommandsImpl(AsyncConnection<String, String> connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    private <K, V> void applyOptions(CommandArgs<K, V> args, CompositeArgument options) {
        if (options != null) {
            options.build(args);
        }
    }

    @Override
    public RedisFuture<Long> tsAdd(String key, long timestamp, double value) {
        Preconditions.checkArgument(timestamp >= 0, "timestamp must be greater than or equal to 0");

        return tsAdd(key, timestamp, value, null);
    }

    public RedisFuture<Long> tsAdd(String key, long timestamp, double value, TsAddArgs options) {
        Preconditions.checkArgument(timestamp >= 0, "timestamp must be greater than or equal to 0");

        Command<String, String, Long> command = this.builder.tsAdd(key, timestamp, value, options);

        return commands().dispatch(command);
    }


    private Dispatcher<String, String> commands() {
        return connection.dispatcher();
    }

    @Override
    public RedisFuture<List<TimestampValuePair>> tsRange(String key, long fromTimestamp, long toTimestamp) {
        Preconditions.checkArgument(fromTimestamp >= 0, "fromTimestamp must be greater than or equal to 0");
        Preconditions.checkArgument(toTimestamp >= 0, "toTimestamp must be greater than or equal to 0");

        Command<String, String, List<TimestampValuePair>> cmd = this.builder.tsRange(key, fromTimestamp, toTimestamp);
        return commands().dispatch(cmd);
    }

    @Override
    public RedisFuture<TimestampValuePair> tsGet(String key) {
        Command<String, String, TimestampValuePair> cmd = this.builder.toGet(key);
        return commands().dispatch(cmd);
    }

    @Override
    public RedisFuture<Long> tsDel(String key, long fromTimestamp, long toTimestamp) {
        Command<String, String, Long> cmd = this.builder.toDel(key, fromTimestamp, toTimestamp);
        return commands().dispatch(cmd);
    }

    @Override
    public RedisFuture<List<TimestampValuePair>> tsRevrange(String key, long fromTimestamp, long toTimestamp) {
        Command<String, String, List<TimestampValuePair>> cmd = this.builder.tsRevrange(key, fromTimestamp, toTimestamp);
        return commands().dispatch(cmd);
    }
}
