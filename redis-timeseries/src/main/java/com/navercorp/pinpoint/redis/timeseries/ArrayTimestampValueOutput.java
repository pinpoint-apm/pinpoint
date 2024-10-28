package com.navercorp.pinpoint.redis.timeseries;

import com.navercorp.pinpoint.redis.timeseries.model.TimestampValuePair;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.output.CommandOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayTimestampValueOutput<K, V> extends CommandOutput<K, V, List<TimestampValuePair>> {

    private int touch;

    private boolean initialized;

    private final TimestampValuePair.Builder builder = TimestampValuePair.newBuilder();

    public ArrayTimestampValueOutput(RedisCodec<K, V> codec) {
        super(codec, Collections.emptyList());
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
        if (depth == 1 && touch >= 2) {
            touch = 0;

            this.output.add(builder.buildAndClear());
        }
    }


    @Override
    public void multi(int count) {
        if (!initialized) {
            output = new ArrayList<>(Math.max(1, count));
            initialized = true;
        }
    }

}
