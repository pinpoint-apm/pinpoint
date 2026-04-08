package com.navercorp.pinpoint.common.util;

public class ArrayCache<K, V> {
    private final Entry<K, V>[] entries;
    private final int capacity;

    public ArrayCache() {
        this(4);
    }

    @SuppressWarnings("unchecked")
    public ArrayCache(int capacity) {
        this.capacity = capacity;
        this.entries = new Entry[capacity];
    }

    public V get(K key) {
        if (key == null) {
            return null;
        }
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> e = entries[i];
            if (e != null && key.equals(e.key)) {
                return e.value;
            }
        }
        return null;
    }

    public void put(K key, V value) {
        if (key == null) {
            return;
        }
        int emptySlot = -1;
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> e = entries[i];
            if (e == null) {
                if (emptySlot == -1) {
                    emptySlot = i;
                }
                continue;
            }
            if (key.equals(e.key)) {
                entries[i] = new Entry<>(key, value);
                return;
            }
        }
        if (emptySlot != -1) {
            entries[emptySlot] = new Entry<>(key, value);
        }
    }

    public void remove(K key) {
        if (key == null) {
            return;
        }
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> e = entries[i];
            if (e != null && key.equals(e.key)) {
                entries[i] = null;
            }
        }
    }

    private static class Entry<K, V> {
        private final K key;
        private final V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}