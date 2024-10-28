package com.navercorp.pinpoint.redis.timeseries.protocol;

import io.lettuce.core.CompositeArgument;
import io.lettuce.core.protocol.CommandArgs;

import java.util.Objects;

public class OnDuplicate implements CompositeArgument  {

    public static OnDuplicate block() {
        return new OnDuplicate(Duplicate.BLOCK);
    }

    public static OnDuplicate last() {
        return new OnDuplicate(Duplicate.LAST);
    }

    public static OnDuplicate first() {
        return new OnDuplicate(Duplicate.FIRST);
    }

    public static OnDuplicate min() {
        return new OnDuplicate(Duplicate.MIN);
    }

    public static OnDuplicate max() {
        return new OnDuplicate(Duplicate.MAX);
    }

    public static OnDuplicate sum() {
        return new OnDuplicate(Duplicate.SUM);
    }


    OnDuplicate(Duplicate duplicate) {
        this.duplicate = Objects.requireNonNull(duplicate, "duplicate");
    }

    protected final Duplicate duplicate;

    @Override
    public <K, V> void build(CommandArgs<K, V> args) {
        args.add("ON_DUPLICATE").add(duplicate.name());

    }
}
