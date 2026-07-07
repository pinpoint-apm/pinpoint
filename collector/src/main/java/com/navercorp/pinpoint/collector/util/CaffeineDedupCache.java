/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Caffeine-based {@link DedupCache}.
 * <p>
 * expireAfterWrite should be the slot length plus a margin; a dedicated
 * scheduler thread evicts expired entries promptly even when traffic stops,
 * and is shut down on {@link #close()}.
 * <p>
 * When enabled via {@code recordStats}, a hit is a deduplicated (saved) write and
 * a miss is an actual write, so {@link CacheStats#hitRate()} is the write-saving
 * ratio and a rising {@link CacheStats#evictionCount()} signals an undersized
 * cache. Disabled by default; {@link #stats()} then returns all zeros.
 */
public class CaffeineDedupCache<K> implements DedupCache<K>, AutoCloseable {

    private final ScheduledExecutorService scheduler;
    private final Cache<K, Boolean> cache;
    private final ConcurrentMap<K, Boolean> view;

    public CaffeineDedupCache(Duration expireAfterWrite, long maximumSize) {
        this(expireAfterWrite, maximumSize, false);
    }

    public CaffeineDedupCache(Duration expireAfterWrite, long maximumSize, boolean recordStats) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                PinpointThreadFactory.createThreadFactory("DedupCache-Scheduler", true));

        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.expireAfterWrite(expireAfterWrite)
                .maximumSize(maximumSize)
                .scheduler(Scheduler.forScheduledExecutorService(scheduler));
        if (recordStats) {
            builder.recordStats();
        }
        this.cache = builder.build();
        this.view = cache.asMap();
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }

    @Override
    public boolean update(K key) {
        Objects.requireNonNull(key, "key");
        // fast path: allocation-free lock-free read; getIfPresent records hit/miss stats
        if (cache.getIfPresent(key) != null) {
            return false;
        }
        // slow path: atomic claim via the view's putIfAbsent, which records no stats --
        // hit/miss stay counted once, so missCount matches the actual writes.
        // When first arrivals race, both record a miss but only the winner writes (minor overcount).
        return view.putIfAbsent(key, Boolean.TRUE) == null;
    }

    public CacheStats stats() {
        return cache.stats();
    }

    public long estimatedSize() {
        return cache.estimatedSize();
    }
}
