package com.navercorp.pinpoint.redis.timeseries.protocol;

import io.lettuce.core.protocol.CommandArgs;

public class DuplicatePolicy extends OnDuplicate {

    public static DuplicatePolicy block() {
        return new DuplicatePolicy(Duplicate.BLOCK);
    }

    public static DuplicatePolicy last() {
        return new DuplicatePolicy(Duplicate.LAST);
    }

    public static DuplicatePolicy first() {
        return new DuplicatePolicy(Duplicate.FIRST);
    }

    public static DuplicatePolicy min() {
        return new DuplicatePolicy(Duplicate.MIN);
    }

    public static DuplicatePolicy max() {
        return new DuplicatePolicy(Duplicate.MAX);
    }

    public static DuplicatePolicy sum() {
        return new DuplicatePolicy(Duplicate.SUM);
    }


    private DuplicatePolicy(Duplicate duplicate) {
        super(duplicate);
    }

    @Override
    public <K, V> void build(CommandArgs<K, V> args) {
        args.add("DUPLICATE_POLICY").add(duplicate.name());

    }
}
