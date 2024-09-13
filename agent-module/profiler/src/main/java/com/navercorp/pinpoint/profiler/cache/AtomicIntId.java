package com.navercorp.pinpoint.profiler.cache;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class AtomicIntId<K> implements Function<K, Integer> {

    private final AtomicInteger idGen = new AtomicInteger(0);

    @Override
    public Integer apply(K key) {
        return idGen.incrementAndGet();
    }
}