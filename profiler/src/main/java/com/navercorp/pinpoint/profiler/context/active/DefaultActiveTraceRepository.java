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

package com.navercorp.pinpoint.profiler.context.active;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ReuseResponseTimeCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Taejin Koo
 */
public class DefaultActiveTraceRepository implements ActiveTraceRepository {

    // memory leak defense threshold
    private static final int DEFAULT_MAX_ACTIVE_TRACE_SIZE = 1024 * 10;
    // oom safe cache
    private final ConcurrentMap<Long, ActiveTrace> activeTraceInfoMap;

    private final ReuseResponseTimeCollector reuseResponseTimeCollector = new ReuseResponseTimeCollector();

    public DefaultActiveTraceRepository() {
        this(DEFAULT_MAX_ACTIVE_TRACE_SIZE);
    }

    public DefaultActiveTraceRepository(int maxActiveTraceSize) {
        this.activeTraceInfoMap = createCache(maxActiveTraceSize);
    }

    private ConcurrentMap<Long, ActiveTrace> createCache(int maxActiveTraceSize) {
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        cacheBuilder.concurrencyLevel(64);
        cacheBuilder.initialCapacity(maxActiveTraceSize);
        cacheBuilder.maximumSize(maxActiveTraceSize);
        // OOM defense
        cacheBuilder.weakValues();

        final Cache<Long, ActiveTrace> localCache = cacheBuilder.build();
        return localCache.asMap();
    }

    @Override
    public void put(ActiveTrace activeTrace) {
        this.activeTraceInfoMap.put(activeTrace.getId(), activeTrace);
    }

    @Override
    public ActiveTrace remove(Long key) {
        ActiveTrace activeTrace = this.activeTraceInfoMap.remove(key);
        if (activeTrace != null) {
            long responseTime = System.currentTimeMillis() - activeTrace.getStartTime();
            reuseResponseTimeCollector.add(responseTime);
        }
        return activeTrace;
    }

    // @ThreadSafe
    @Override
    public List<ActiveTraceInfo> collect() {
        final Collection<ActiveTrace> copied = this.activeTraceInfoMap.values();
        if (copied.isEmpty()) {
            return Collections.emptyList();
        }

        final List<ActiveTraceInfo> collectData = new ArrayList<ActiveTraceInfo>(copied.size());
        for (ActiveTrace trace : copied) {
            final long startTime = trace.getStartTime();
            // not started
            if (startTime > 0) {
                final ActiveTraceInfo snapshot = snapshot(trace);
                collectData.add(snapshot);
            }
        }
        return collectData;
    }

    private ActiveTraceInfo snapshot(ActiveTrace trace) {
        if (trace.isSampled()) {
            return new ActiveTraceInfo(trace.getId(), trace.getStartTime(), trace.getBindThread(), true, trace.getTransactionId(), trace.getEntryPoint());
        } else {
            // clear Trace reference
            return new ActiveTraceInfo(trace.getId(), trace.getStartTime(), trace.getBindThread());
        }
    }

    @Override
    public ResponseTimeValue getLatestCompletedActiveTraceResponseTimeValue() {
        return reuseResponseTimeCollector.resetAndGetValue();
    }

}
