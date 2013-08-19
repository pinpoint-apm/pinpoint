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

    private final Entry<T>[] entryArray;

    public ConcurrentCounterMap() {
        this(16);
    }

    public ConcurrentCounterMap(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
        this.entryArray = createEntry();
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
        final int mod = Math.abs(entrySelector.getAndIncrement() % concurrencyLevel);
        return entryArray[mod];
    }

    public void increment(T key, Long increment) {
        Entry<T> entry = getEntry();
        entry.increment(key, increment);
    }

    public Map<T, LongAdder> remove() {
        // copy 최대한 근처의 정합성을 맞추가 위해서 먼저 한번에 copy한다.
        List<Map<T, LongAdder>> copy = new ArrayList<Map<T, LongAdder>>(entryArray.length);
        for(int i = 0; i < entryArray.length; i++ ) {
            Entry<T> tEntry = entryArray[i];
            Map<T, LongAdder> remove = tEntry.remove();
            copy.add(remove);
        }

        // merge
        Map<T, LongAdder> mergeMap = new HashMap<T, LongAdder>();
        for (Map<T, LongAdder> mutableLongMap : copy) {
            for (Map.Entry<T, LongAdder> entry : mutableLongMap.entrySet()) {
                LongAdder longAdder = mergeMap.get(entry.getKey());
                if (longAdder == null) {
                    mergeMap.put(entry.getKey(), entry.getValue());
                } else {
                    longAdder.increment(entry.getValue().get());
                }
            }
        }
        return mergeMap;
    }


    public static class LongAdder {
        private long value = 0;

        public LongAdder(long increase) {
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
        private Map<T, LongAdder> map = new HashMap<T, LongAdder>();

        public synchronized void increment(T key, Long increment) {
            LongAdder longAdder = map.get(key);
            if (longAdder == null) {
                map.put(key, new LongAdder(increment));
            } else {
                longAdder.increment(increment);
            }
        }

        public Map<T, LongAdder> remove() {
            Map<T, LongAdder> old = null;
            synchronized (this) {
                old = this.map;
                this.map = new HashMap<T, LongAdder>();
            }
            return old;
        }
    }
}
