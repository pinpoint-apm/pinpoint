package com.navercorp.pinpoint.redis.timeseries;

import com.navercorp.pinpoint.redis.timeseries.model.TimestampValuePair;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.output.CommandOutput;

public class MetricOutput<K, V> extends CommandOutput<K, V, TimestampValuePair> {

    private final TimestampValuePair.Builder builder = TimestampValuePair.newBuilder();
    private int touch;

    public MetricOutput(RedisCodec<K, V> codec) {
        super(codec, null);
    }

    @Override
    public void set(long integer) {
        this.builder.timestamp(integer);
        touch();
    }

    private void touch() {
        touch++;
    }

    @Override
    public void set(double number) {
        this.builder.value(number);
        touch();
    }


    @Override
    public void complete(int depth) {
        if (depth == 1 && touch == 2) {
            this.output = builder.buildAndClear();
        }
    }


    @Override
    public void multi(int count) {
    }

}
