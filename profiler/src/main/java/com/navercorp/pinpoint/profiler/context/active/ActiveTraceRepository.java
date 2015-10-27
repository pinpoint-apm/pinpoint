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
import com.navercorp.pinpoint.profiler.context.ActiveTrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Taejin Koo
 */
public class ActiveTraceRepository implements ActiveTraceLocator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // memory leak defense threshold
    private static final int DEFAULT_MAX_ACTIVE_TRACE_SIZE = 1024 * 10;
    // oom safe cache
    private final ConcurrentMap<Long, ActiveTrace> activeTraceInfoMap;

    public ActiveTraceRepository() {
        this(DEFAULT_MAX_ACTIVE_TRACE_SIZE);
    }
    public ActiveTraceRepository(int maxActiveTraceSize) {
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

    public void put(ActiveTrace activeTrace) {
        this.activeTraceInfoMap.put(activeTrace.getId(), activeTrace);
    }

    private ActiveTrace get(Long key) {
        return this.activeTraceInfoMap.get(key);
    }


    // @ThreadSafe
    public Object getStackTrace(Long key) {
        final ActiveTrace trace = get(key);
        if (trace == null) {
            return null;
        }

        final Thread bindThread = trace.getBindThread();
        if (bindThread == null) {
            return null;
        }
        // TODO sudo code
        StackTraceElement[] stackTrace = bindThread.getStackTrace();
        logger.info("stackTrace:{}", Arrays.toString(stackTrace));

//      copy TraceCallStack data
//        CallStack callStack = trace.copyCallStack();
        return null;
    }

    public ActiveTrace remove(Long key) {
        return this.activeTraceInfoMap.remove(key);
    }

    // @ThreadSafe
    @Override
    public List<ActiveTraceInfo> collect() {
        List<ActiveTraceInfo> collectData = new ArrayList<ActiveTraceInfo>();
        final Collection<ActiveTrace> copied = this.activeTraceInfoMap.values();
        for (ActiveTrace trace : copied) {
            final long startTime = trace.getStartTime();
            // not started
            if (startTime > 0) {
                // clear Trace reference
                ActiveTraceInfo activeTraceInfo = new ActiveTraceInfo(trace.getId(), startTime, trace.getBindThread());
                collectData.add(activeTraceInfo);
            }
        }
        return collectData;
    }
}
