package com.navercorp.pinpoint.metric.collector;


import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Set;

public class TypeSet {
    private final Set<CollectorType> types;

    public TypeSet(Set<CollectorType> types) {
        this.types = Objects.requireNonNull(types, "types");
    }


    public boolean hasType(CollectorType type) {
        Objects.requireNonNull(type, "type");

        for (CollectorType collectorType : types) {
            if (collectorType.hasType(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return StringUtils.join(types, ",");
    }
}
