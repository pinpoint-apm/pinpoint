package com.navercorp.pinpoint.redis.timeseries;

import com.google.common.base.Preconditions;
import com.navercorp.pinpoint.redis.timeseries.model.TimestampValuePair;
import com.navercorp.pinpoint.redis.timeseries.protocol.TS;
import io.lettuce.core.CompositeArgument;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.IntegerOutput;
import io.lettuce.core.protocol.CommandArgs;

import java.util.List;
import java.util.Objects;

public class RedisTimeseriesCommandsImpl implements RedisTimeseriesCommands {

    private final RedisCodec<String, String> commandCodec = StringCodec.ASCII;

    private final RedisCodec<String, String> outputCodec = StringCodec.UTF8;

    private final TimeseriesCommandBuilder<String, String> command = new TimeseriesCommandBuilder(commandCodec);

    private final StatefulRedisConnection<String, String> connection;

    public RedisTimeseriesCommandsImpl(RedisClient client) {
        Objects.requireNonNull(client, "client");
        this.connection = client.connect();
    }

    @Override
    public long tsAdd(String key, long timestamp, double value) {
       return tsAdd(key, timestamp, value, null);
    }


    @Override
    public long tsDel(String key, long fromTimestamp, long toTimestamp) {
        Preconditions.checkArgument(fromTimestamp >= 0, "fromTimestamp must be greater than or equal to 0");

        CommandArgs<String, String> args = new CommandArgs<>(commandCodec)
                .addKey(key)
                .add(fromTimestamp)
                .add(toTimestamp);

        RedisCommands<String, String> commands = connection.sync();
        return commands.dispatch(TS.DEL, new IntegerOutput<>(outputCodec), args);
    }

    @Override
    public long tsAdd(String key, long timestamp, double value, TsAddArgs options) {
        Preconditions.checkArgument(timestamp >= 0, "timestamp must be greater than or equal to 0");

        CommandArgs<String, String> args = new CommandArgs<>(commandCodec)
                .addKey(key)
                .add(timestamp)
                .add(value);

        applyOptions(args, options);

        RedisCommands<String, String> commands = connection.sync();
        return commands.dispatch(TS.ADD, new IntegerOutput<>(outputCodec), args);
    }

    private <K, V> void applyOptions(CommandArgs<K, V> args, CompositeArgument options) {
        if (options != null) {
            options.build(args);
        }
    }

    @Override
    public List<TimestampValuePair> tsRange(String key, long fromTimestamp, long toTimestamp) {
        return tsRangeCommand(TS.RANGE, key, fromTimestamp, toTimestamp);
    }

    @Override
    public TimestampValuePair tsGet(String key) {
        CommandArgs<String, String> args = new CommandArgs<>(commandCodec)
                .addKey(key);
        RedisCommands<String, String> commands = connection.sync();
        return commands.dispatch(TS.GET, new MetricOutput<>(outputCodec), args);
    }

    @Override
    public List<TimestampValuePair> tsRevrange(String key, long fromTimestamp, long toTimestamp) {
        return tsRangeCommand(TS.REVRANGE, key, fromTimestamp, toTimestamp);
    }

    private List<TimestampValuePair> tsRangeCommand(TS command, String key, long fromTimestamp, long toTimestamp) {
        Preconditions.checkArgument(fromTimestamp >= 0, "fromTimestamp must be greater than or equal to 0");
        Preconditions.checkArgument(toTimestamp >= 0, "toTimestamp must be greater than or equal to 0");

        CommandArgs<String, String> args = new CommandArgs<>(commandCodec)
                .addKey(key)
                .add(fromTimestamp)
                .add(toTimestamp);

        RedisCommands<String, String> commands = connection.sync();
        return commands.dispatch(command, new ArrayTimestampValueOutput<>(outputCodec), args);
    }


}
