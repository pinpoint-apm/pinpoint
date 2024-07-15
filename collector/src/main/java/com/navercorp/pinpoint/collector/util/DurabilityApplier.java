package com.navercorp.pinpoint.collector.util;

import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Mutation;

import java.util.Objects;

public class DurabilityApplier {
    private final Durability durability;

    public DurabilityApplier(String durability) {
        this(durability, Durability.USE_DEFAULT);
    }

    public DurabilityApplier(String durability, Durability defaultDurability) {
        this.durability = toDurability(durability, defaultDurability);
    }

    public static Durability toDurability(String durability, Durability defaultDurability) {
        Objects.requireNonNull(defaultDurability, "defaultDurability");

        if (StringUtils.isEmpty(durability)) {
            return defaultDurability;
        }
        try {
            return Durability.valueOf(durability.toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultDurability;
        }
    }

    public void apply(Mutation mutation) {
        if (mutation == null) {
            return;
        }
        mutation.setDurability(durability);
    }

    public Durability getDurability() {
        return durability;
    }

    @Override
    public String toString() {
        return "DurabilityConfiguration{" +
                "durability=" + durability +
                '}';
    }
}
