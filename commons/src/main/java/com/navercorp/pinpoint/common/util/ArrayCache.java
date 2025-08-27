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
        for (int i = 0; i < capacity; i++) {
            entries[i] = new Entry<>();
        }
    }

    public V get(K key) {
        if (key == null) {
            return null;
        }
        for (Entry<K, V> e : entries) {
            if (key.equals(e.key)) {
                return e.value;
            }
        }
        return null;
    }

    public void put(K key, V value) {
        if (key == null) {
            return;
        }
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> e = entries[i];
            if (e.key == null) {
                synchronized (e) {
                    if (e.key == null) {
                        e.key = key;
                        e.value = value;
                        return;
                    }
                }
            }
            if (e.key.equals(key)) {
                e.value = value;
                return;
            }
        }
    }

    public void remove(K key) {
        if (key == null) {
            return;
        }
        for (int i = 0; i < capacity; i++) {
            Entry<K, V> e = entries[i];
            if (key.equals(e.key)) {
                synchronized (e) {
                    if (key.equals(e.key)) {
                        e.key = null;
                        e.value = null;
                        return;
                    }
                }
            }
        }
    }


    private static class Entry<K, V> {
        K key;
        V value;
    }
}
