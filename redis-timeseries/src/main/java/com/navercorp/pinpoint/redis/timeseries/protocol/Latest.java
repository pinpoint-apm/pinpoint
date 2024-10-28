package com.navercorp.pinpoint.redis.timeseries.protocol;

import io.lettuce.core.CompositeArgument;
import io.lettuce.core.protocol.CommandArgs;

public class Latest implements CompositeArgument {

    public Latest latest() {
        return new Latest();
    }

    @Override
    public <K, V> void build(CommandArgs<K, V> args) {
        args.add("LATEST");
    }
}
