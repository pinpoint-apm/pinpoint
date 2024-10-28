package com.navercorp.pinpoint.redis.timeseries.protocol;

import io.lettuce.core.CompositeArgument;
import io.lettuce.core.protocol.CommandArgs;

public class Count implements CompositeArgument {
    private final long count;

    public Count(long count) {
        this.count = count;
    }

    @Override
    public <K, V> void build(CommandArgs<K, V> args) {
        args.add("COUNT").add(count);
    }
}
