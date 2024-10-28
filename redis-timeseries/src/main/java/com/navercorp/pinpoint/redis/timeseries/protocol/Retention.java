package com.navercorp.pinpoint.redis.timeseries.protocol;

import io.lettuce.core.CompositeArgument;
import io.lettuce.core.protocol.CommandArgs;

import java.util.concurrent.TimeUnit;

public class Retention implements CompositeArgument {

    private final long retentionPeriod;

    private Retention(long retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
    }

    public static Retention of(long retentionPeriod) {
        return new Retention(retentionPeriod);
    }
    public static Retention of(long retentionPeriod, TimeUnit timeUnit) {
        return new Retention(timeUnit.toMillis(retentionPeriod));
    }

    @Override
    public <K, V> void build(CommandArgs<K, V> args) {
        args.add("RETENTION").add(retentionPeriod);
    }

    @Override
    public String toString() {
        return "Retention{" +
                retentionPeriod +
                '}';
    }
}
