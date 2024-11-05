package com.navercorp.pinpoint.redis.timeseries;

import com.navercorp.pinpoint.redis.timeseries.model.TimestampValuePair;
import com.navercorp.pinpoint.redis.timeseries.protocol.TS;
import io.lettuce.core.CompositeArgument;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.output.CommandOutput;
import io.lettuce.core.output.IntegerOutput;
import io.lettuce.core.protocol.BaseRedisCommandBuilder;
import io.lettuce.core.protocol.Command;
import io.lettuce.core.protocol.CommandArgs;

import java.util.List;

public class TimeseriesCommandBuilder<K, V> extends BaseRedisCommandBuilder<K, V> {

    public TimeseriesCommandBuilder(RedisCodec<K, V> codec) {
        super(codec);
    }

    public Command<K, V, Long> tsAdd(K key, long timestamp, double value, TsAddArgs options) {
        CommandArgs<K, V> args = new CommandArgs<>(codec)
                .addKey(key)
                .add(timestamp)
                .add(value);

        applyOptions(args, options);

        CommandOutput<K, V, Long> output = new IntegerOutput<>(codec);

        return tsCommand(TS.ADD, output, args);
    }

    private <T> Command<K, V, T> tsCommand(TS ts, CommandOutput<K, V, T> output, CommandArgs<K, V> args) {
        return new Command<>(ts, output, args);
    }


    private <K, V> void applyOptions(CommandArgs<K, V> args, CompositeArgument options) {
        if (options != null) {
            options.build(args);
        }
    }

    public Command<K, V, List<TimestampValuePair>> tsGet(K key) {
        CommandArgs<K, V> args = new CommandArgs<>(codec)
                .addKey(key);
        ArrayTimestampValueOutput<K, V> output = new ArrayTimestampValueOutput<>(codec);
        return tsCommand(TS.GET, output, args);
    }


    public Command<K, V, List<TimestampValuePair>> tsRange(K key, long fromTimestamp, long toTimestamp) {
        return tsRange(TS.RANGE, key, fromTimestamp, toTimestamp);
    }

    public Command<K, V, List<TimestampValuePair>> tsRevrange(K key, long fromTimestamp, long toTimestamp) {
        return tsRange(TS.REVRANGE, key, fromTimestamp, toTimestamp);
    }

    public Command<K, V, List<TimestampValuePair>> tsRange(TS ts, K key, long fromTimestamp, long toTimestamp) {
        CommandArgs<K, V> args = new CommandArgs<>(codec)
                .addKey(key)
                .add(fromTimestamp)
                .add(toTimestamp);
        ArrayTimestampValueOutput<K, V> output = new ArrayTimestampValueOutput<>(codec);
        return tsCommand(ts, output, args);
    }

    public Command<K, V, Long> toDel(K key, long fromTimestamp, long toTimestamp) {
        CommandArgs<K, V> args = new CommandArgs<>(codec)
                .addKey(key)
                .add(fromTimestamp)
                .add(toTimestamp);

        return tsCommand(TS.DEL, new IntegerOutput<>(codec), args);
    }

    public Command<K, V, TimestampValuePair> toGet(K key) {
        CommandArgs<K, V> args = new CommandArgs<>(codec)
                .addKey(key);
        return tsCommand(TS.GET, new MetricOutput<>(codec), args);
    }
}
