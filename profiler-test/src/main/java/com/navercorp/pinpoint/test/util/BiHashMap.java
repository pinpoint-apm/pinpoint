package com.navercorp.pinpoint.test.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * copy & modify org.redisson.misc.BiHashMap
 * https://github.com/redisson/redisson/blob/master/redisson/src/main/java/org/redisson/misc/BiHashMap.java
 * @param <K>
 * @param <V>
 */
public class BiHashMap<K, V> {
    private final Map<K, V> keyValueMap = new HashMap<>();
    private final Map<V, K> valueKeyMap = new HashMap<>();

    public int size() {
        return this.keyValueMap.size();
    }

    public boolean isEmpty() {
        return this.keyValueMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        return this.keyValueMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.valueKeyMap.containsKey(value);
    }

    public V get(Object key) {
        return this.keyValueMap.get(key);
    }

    public K reverseGet(Object key) {
        return this.valueKeyMap.get(key);
    }

    public V put(K key, V value) {
        // modify
        // replace key
        if (this.keyValueMap.containsKey(key)) {
            this.valueKeyMap.remove(this.keyValueMap.get(key));

            return put0(key, value);
        }
        // replace value
        if (this.valueKeyMap.containsKey(value)) {
            this.keyValueMap.remove(this.valueKeyMap.get(value));

            return put0(key, value);
        }

        return put0(key, value);
    }

    private V put0(K key, V value) {
        this.valueKeyMap.put(value, key);
        return this.keyValueMap.put(key, value);
    }

    public V remove(Object key) {
        V removed = this.keyValueMap.remove(key);
        if (removed != null) {
            this.valueKeyMap.remove(removed);
        }

        return removed;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        Iterator var2 = m.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<? extends K, ? extends V> entry = (Map.Entry)var2.next();
            this.put(entry.getKey(), entry.getValue());
        }

    }

    public void clear() {
        this.keyValueMap.clear();
        this.valueKeyMap.clear();
    }

    public Set<K> keySet() {
        return this.keyValueMap.keySet();
    }

    public Set<V> valueSet() {
        return this.valueKeyMap.keySet();
    }

    public Collection<V> values() {
        return this.keyValueMap.values();
    }

    public Collection<K> keys() {
        return this.valueKeyMap.values();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return this.keyValueMap.entrySet();
    }

    public Set<Map.Entry<V, K>> reverseEntrySet() {
        return this.valueKeyMap.entrySet();
    }

}
