package com.navercorp.pinpoint.redis.timeseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OutputFactory {
    static <T> List<T> newList(int capacity) {

        if (capacity < 1) {
            return Collections.emptyList();
        }

        return new ArrayList<>(Math.max(1, capacity));
    }

    static <V> Set<V> newSet(int capacity) {

        if (capacity < 1) {
            return Collections.emptySet();
        }

        return new LinkedHashSet<>(capacity, 1);
    }
}
