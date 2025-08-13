package com.navercorp.pinpoint.common.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class RingMapCache<K, V> {
    private final int capacity;
    private final ConcurrentHashMap<K, V> map;
    private final AtomicReferenceArray<K> keys;
    private final AtomicInteger writeIndex = new AtomicInteger(0);

    public RingMapCache() {
        this(256);
    }

    public RingMapCache(int inputCapacity) {
        this.capacity = nextPowerOfTwo(inputCapacity);
        this.map = new ConcurrentHashMap<>(capacity);
        this.keys = new AtomicReferenceArray<>(capacity);
    }

    public V get(K key) {
        return map.get(key);
    }

    public void putIfAbsent(K key, V value) {
        V existing = map.putIfAbsent(key, value);
        if (existing != null) {
            return;
        }

        int idx = nextIndex(writeIndex.getAndIncrement());
        K oldKey = keys.getAndSet(idx, key);
        if (oldKey != null) {
            map.remove(oldKey);
        }
    }

    private int nextPowerOfTwo(int n) {
        int v = 1;
        while (v < n) {
            v <<= 1;
        }
        return v;
    }

    private int nextIndex(int rawIndex) {
        return rawIndex & (capacity - 1);
    }

}
