/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.profiler.cache.CaffeineBuilder;
import com.navercorp.pinpoint.profiler.context.active.ActiveTrace;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.DefaultActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.EmptyActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.module.config.TraceAgentActiveThread;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeCollector;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ActiveTraceRepositoryProvider implements Provider<ActiveTraceRepository> {
    // memory leak defense threshold
    private static final int DEFAULT_MAX_ACTIVE_TRACE_SIZE = 1024 * 10;

    private final boolean isTraceAgentActiveThread;
    private final ResponseTimeCollector responseTimeCollector;

    @Inject
    public ActiveTraceRepositoryProvider(@TraceAgentActiveThread boolean isTraceAgentActiveThread, ResponseTimeCollector responseTimeCollector) {
        this.isTraceAgentActiveThread = isTraceAgentActiveThread;
        this.responseTimeCollector = Objects.requireNonNull(responseTimeCollector, "responseTimeCollector");

    }

    public ActiveTraceRepository get() {
        if (isTraceAgentActiveThread) {
            final ConcurrentMap<ActiveTraceHandle, ActiveTrace> activeTraceInfoMap = newActiveTraceMap(DEFAULT_MAX_ACTIVE_TRACE_SIZE);
            return new DefaultActiveTraceRepository(responseTimeCollector, activeTraceInfoMap);
        }
        return new EmptyActiveTraceRepository(responseTimeCollector);
    }

    /**
     * Bounded, oom safe map: traces that are never closed are evicted once the bound is reached.
     */
    private ConcurrentMap<ActiveTraceHandle, ActiveTrace> newActiveTraceMap(int maxActiveTraceSize) {
        final Caffeine<Object, Object> cacheBuilder = CaffeineBuilder.newBuilder();
        cacheBuilder.initialCapacity(maxActiveTraceSize);
        cacheBuilder.maximumSize(maxActiveTraceSize);

        final Cache<ActiveTraceHandle, ActiveTrace> localCache = cacheBuilder.build();
        return localCache.asMap();
    }
}
