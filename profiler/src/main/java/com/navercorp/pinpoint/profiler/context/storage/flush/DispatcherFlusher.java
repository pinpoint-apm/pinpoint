/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.context.storage.flush;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;

/**
 * @author Taejin Koo
 */
public class DispatcherFlusher implements StorageFlusher {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile boolean closed;

    private final List<SimpleEntry<SpanChunkFlushCondition, StorageFlusher>> spanChunkFlusherRepository
            = new ArrayList<SimpleEntry<SpanChunkFlushCondition, StorageFlusher>>();

    private final List<SimpleEntry<SpanFlushCondition, StorageFlusher>> spanFlusherRepository
            = new ArrayList<SimpleEntry<SpanFlushCondition, StorageFlusher>>();

    private final StorageFlusher defaultFlusher;

    public DispatcherFlusher(StorageFlusher defaultFlusher) {
        if (defaultFlusher == null) {
            throw new NullPointerException("defaultFlusher may not be null");
        }

        this.defaultFlusher = defaultFlusher;
    }

    public void addFlusherCondition(FlushCondition condition, StorageFlusher flusher) {
        if (closed) {
            logger.warn("Already closed.");
            return;
        }

        if (flusher == null) {
            throw new NullPointerException("flusher may not be null");
        }

        if (condition instanceof SpanChunkFlushCondition) {
            spanChunkFlusherRepository.add(new SimpleEntry<SpanChunkFlushCondition, StorageFlusher>((SpanChunkFlushCondition) condition, flusher));
        }

        if (condition instanceof SpanFlushCondition) {
            spanFlusherRepository.add(new SimpleEntry<SpanFlushCondition, StorageFlusher>((SpanFlushCondition) condition, flusher));
        }
    }

    @Override
    public void flush(SpanChunk spanChunk) {
        if (closed) {
            logger.warn("Already closed.");
            return;
        }

        for (SimpleEntry<SpanChunkFlushCondition, StorageFlusher> entry : spanChunkFlusherRepository) {
            SpanChunkFlushCondition condition = entry.getKey();
            StorageFlusher flusher = entry.getValue();
            if (condition.matches(spanChunk, flusher)) {
                flusher.flush(spanChunk);
                return;
            }
        }

        defaultFlusher.flush(spanChunk);
    }

    @Override
    public void flush(Span span) {
        if (closed) {
            logger.warn("Already closed.");
            return;
        }

        for (SimpleEntry<SpanFlushCondition, StorageFlusher> entry : spanFlusherRepository) {
            SpanFlushCondition condition = entry.getKey();
            StorageFlusher flusher = entry.getValue();
            if (condition.matches(span, flusher)) {
                flusher.flush(span);
                return;
            }
        }

        defaultFlusher.flush(span);
    }

    @Override
    public void stop() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }

        Set<StorageFlusher> storageFlusherSet = new HashSet<StorageFlusher>();
        for (SimpleEntry<SpanChunkFlushCondition, StorageFlusher> entry : spanChunkFlusherRepository) {
            storageFlusherSet.add(entry.getValue());
        }
        for (SimpleEntry<SpanFlushCondition, StorageFlusher> entry : spanFlusherRepository) {
            storageFlusherSet.add(entry.getValue());
        }

        for (StorageFlusher flusher : storageFlusherSet) {
            if (flusher != null) {
                flusher.stop();
            }
        }

        defaultFlusher.stop();
    }

    static class SimpleEntry<K, V> implements Map.Entry<K, V> {

        private final K key;

        private V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry<?, ?>)) {
                return false;
            }
            @SuppressWarnings("rawtypes")
            Map.Entry e = (Map.Entry) o;
            return eq(key, e.getKey()) && eq(value, e.getValue());
        }

        @Override
        public int hashCode() {
            return (key == null? 0 : key.hashCode()) ^ (value == null? 0 : value.hashCode());
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        private static boolean eq(Object o1, Object o2) {
            return o1 == null? o2 == null : o1.equals(o2);
        }
    }

}
