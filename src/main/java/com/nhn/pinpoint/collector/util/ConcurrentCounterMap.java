package com.nhn.pinpoint.collector.util;

import com.nhn.pinpoint.common.util.MathUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class ConcurrentCounterMap<T> {

    private final int concurrencyLevel;

    private final AtomicInteger entrySelector = new AtomicInteger(0);

    private final Entry<T>[] entryArray;

    private static final Map EMPTY = Collections.emptyMap();

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
        final int mod = MathUtils.fastAbs(entrySelector.getAndIncrement()) % concurrencyLevel;
        return entryArray[mod];
    }

    public void increment(T key, Long increment) {
        Entry<T> entry = getEntry();
        entry.increment(key, increment);
    }

    public Map<T, LongAdder> remove() {
        // copy 최대한 근처의 정합성을 맞추가 위해서 먼저 한번에 copy한다.
        final List<Map<T, LongAdder>> copy = removeAll();

        // merge
        final Map<T, LongAdder> mergeMap = new HashMap<T, LongAdder>();
        for (Map<T, LongAdder> mutableLongMap : copy) {
            for (Map.Entry<T, LongAdder> entry : mutableLongMap.entrySet()) {
                final T key = entry.getKey();
                LongAdder longAdder = mergeMap.get(key);
                if (longAdder == null) {
                    mergeMap.put(key, entry.getValue());
                } else {
                    longAdder.increment(entry.getValue().get());
                }
            }
        }
        return mergeMap;
    }

    private List<Map<T, LongAdder>> removeAll() {
        final List<Map<T, LongAdder>> copy = new ArrayList<Map<T, LongAdder>>(entryArray.length);
        for(int i = 0; i < entryArray.length; i++ ) {
            Entry<T> tEntry = entryArray[i];
            Map<T, LongAdder> remove = tEntry.remove();
            copy.add(remove);
        }
        return copy;
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
            Map<T, LongAdder> old;
            synchronized (this) {
                old = this.map;
                if (old.isEmpty()) {
                    return EMPTY;
                }
                this.map = new HashMap<T, LongAdder>();
            }
            return old;
        }
    }
}
