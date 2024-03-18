package com.navercorp.pinpoint.profiler.cache;

public interface Cache<K, V> {
    V put(K key);
}
