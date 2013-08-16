package com.nhn.pinpoint.collector.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class ConcurrentCounterMap<T> {

    private final int concurrencyLevel;

    private final AtomicInteger entrySelector = new AtomicInteger(0);

    private final Entry<T>[] entryArray = createEntry();

    public ConcurrentCounterMap() {
        this(16);
    }

    public ConcurrentCounterMap(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
    }

    private Entry<T>[] createEntry() {
        final int concurrencyLevel = this.concurrencyLevel;

        final Entry<T>[] entry = new Entry[concurrencyLevel];
        for (int i = 0; i < entry.length; i++) {
            entry[i] = new Entry<T>();
        }
        return entry;
    }

    private Entry<T> getEntry() {
        final int mod = entrySelector.getAndIncrement() % concurrencyLevel;
        return entryArray[mod];
    }

    public void increment(T key, Long increment) {
        Entry<T> entry = getEntry();
        entry.increment(key, increment);
    }

    public Map<T, MutableLong> remove() {
        // copy 최대한 근처의 정합성을 맞추가 위해서 먼저 한번에 copy한다.
        List<Map<T, MutableLong>> copy = new ArrayList<Map<T, MutableLong>>(entryArray.length);
        for(int i = 0; i < entryArray.length; i++ ) {
            Entry<T> tEntry = entryArray[i];
            Map<T, MutableLong> remove = tEntry.remove();
            copy.add(remove);
        }

        // merge
        Map<T, MutableLong> mergeMap = new HashMap<T, MutableLong>();
        for (Map<T, MutableLong> mutableLongMap : copy) {
            for (Map.Entry<T, MutableLong> entry : mutableLongMap.entrySet()) {
                MutableLong mutableLong = mergeMap.get(entry.getKey());
                if (mutableLong == null) {
                    mergeMap.put(entry.getKey(), entry.getValue());
                } else {
                    mutableLong.increment(entry.getValue().get());
                }
            }
        }
        return mergeMap;
    }


    public static class MutableLong {
        private long value = 0;

        public MutableLong(long increase) {
            this.value = increase;
        }

        public void increment(long increment) {
            this.value += increment;
        }

        public long get() {
            return this.value;
        }
    }

    private static class Entry<T> {
        private Map<T, MutableLong> map = new HashMap<T, MutableLong>();

        public synchronized void increment(T key, Long increment) {
            MutableLong mutableLong = map.get(key);
            if (mutableLong == null) {
                map.put(key, new MutableLong(increment));
            } else {
                mutableLong.increment(increment);
            }
        }

        public Map<T, MutableLong> remove() {
            Map<T, MutableLong> old = null;
            synchronized (this) {
                old = this.map;
                this.map = new HashMap<T, MutableLong>();
            }
            return old;
        }
    }
}
