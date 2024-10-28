package com.navercorp.pinpoint.redis.timeseries.protocol;

import io.lettuce.core.CompositeArgument;
import io.lettuce.core.protocol.CommandArgs;

public class Ignore implements CompositeArgument {
    private final long ignoreMaxTimediff;
    private final long ignoreMaxValDiff;

    public Ignore(long ignoreMaxTimediff, long ignoreMaxValDiff) {
        this.ignoreMaxTimediff = ignoreMaxTimediff;
        this.ignoreMaxValDiff = ignoreMaxValDiff;
    }

    @Override
    public <K, V> void build(CommandArgs<K, V> args) {
        args.add("IGNORE").add(ignoreMaxTimediff).add(ignoreMaxValDiff);
    }
}
