package com.navercorp.pinpoint.redis.timeseries.protocol;

import io.lettuce.core.CompositeArgument;
import io.lettuce.core.protocol.CommandArgs;

import java.util.ArrayList;
import java.util.List;

public class Labels implements CompositeArgument {
    private final List<Label> labels = new ArrayList<>();

    public void addLabel(String label, String value) {
        labels.add(new Label(label, value));
    }

    @Override
    public <K, V> void build(CommandArgs<K, V> args) {
        args.add("LABELS");
        for (Label label : labels) {
            args.add(label.label()).add(label.value());
        }
    }
}
