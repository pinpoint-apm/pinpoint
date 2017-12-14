/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.util;

import com.navercorp.pinpoint.common.util.MathUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @deprecated Since 1.7.0. Use {@link com.google.common.util.concurrent.AtomicLongMap}
 * @author emeroad
 */
@Deprecated
public class ConcurrentCounterMap<T> {

    private final int concurrencyLevel;

    private final AtomicInteger entrySelector;

    private final Entry<T>[] entryArray;

    public ConcurrentCounterMap() {
        this(16);
    }

    public ConcurrentCounterMap(int concurrencyLevel) {
        this(concurrencyLevel, 0);
    }

    public ConcurrentCounterMap(int concurrencyLevel, int entrySelectorId) {
        this.concurrencyLevel = concurrencyLevel;
        this.entryArray = createEntry();
        this.entrySelector = new AtomicInteger(entrySelectorId);
    }

    private Entry<T>[] createEntry() {
        final int concurrencyLevel = this.concurrencyLevel;

        final Entry<T>[] entry = new Entry[concurrencyLevel];
        for (int i = 0; i < entry.length; i++) {
            entry[i] = new Entry<>();
        }
        return entry;
    }

    private Entry<T> getEntry() {
        final int selectKey = MathUtils.fastAbs(entrySelector.getAndIncrement());
        final int mod = selectKey % concurrencyLevel;
        return entryArray[mod];
    }

    public void increment(T key, Long increment) {
        Entry<T> entry = getEntry();
        entry.increment(key, increment);
    }

    public Map<T, LongAdder> remove() {
        // make a copy of the current snapshot of the entries for consistency
        final List<Map<T, LongAdder>> copy = removeAll();

        // merge
        final Map<T, LongAdder> mergeMap = new HashMap<>();
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
        final List<Map<T, LongAdder>> copy = new ArrayList<>(entryArray.length);
        final int entryArrayLength = entryArray.length;
        for (int i = 0; i < entryArrayLength; i++ ) {
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
        private static final Map EMPTY = Collections.emptyMap();


        private Map<T, LongAdder> map = new HashMap<>();

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
                this.map = new HashMap<>();
            }
            return old;
        }
    }
}
