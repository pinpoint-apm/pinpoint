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

package com.navercorp.pinpoint.profiler.context;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
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
public class ActiveTraceRepository {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // memory leak defense threshold
    private static final int DEFAULT_MAX_ACTIVE_TRACE_SIZE = 1024 * 10;
    // oom safe cache
    private final ConcurrentMap<Long, Trace> activeTraceInfoMap;

    public ActiveTraceRepository() {
        this(DEFAULT_MAX_ACTIVE_TRACE_SIZE);
    }
    public ActiveTraceRepository(int maxActiveTraceSize) {
        this.activeTraceInfoMap = createCache(maxActiveTraceSize);
    }

    private ConcurrentMap<Long, Trace> createCache(int maxActiveTraceSize) {
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        cacheBuilder.concurrencyLevel(64);
        cacheBuilder.initialCapacity(maxActiveTraceSize);
        cacheBuilder.maximumSize(maxActiveTraceSize);
        // OOM defense
        cacheBuilder.weakValues();

        final Cache<Long, Trace> localCache = cacheBuilder.build();
        return localCache.asMap();
    }

    public void put(Long key, Trace trace) {
        this.activeTraceInfoMap.put(key, trace);
    }

    private Trace get(Long key) {
        return this.activeTraceInfoMap.get(key);
    }


    // @ThreadSafe
    public Object getStackTrace(Long key) {
        final Trace trace = get(key);
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

    public Trace remove(Long key) {
        return this.activeTraceInfoMap.remove(key);
    }

    // @ThreadSafe
    public List<ActiveTraceInfo> collect() {
        List<ActiveTraceInfo> collectData = new ArrayList<ActiveTraceInfo>();
        final Collection<Trace> copy = this.activeTraceInfoMap.values();
        for (Trace trace : copy) {
            final long startTime = trace.getStartTime();
            // not started
            if (startTime > 0) {
                // clear Trace reference
                ActiveTraceInfo activeTraceInfo = new ActiveTraceInfo(startTime);
                collectData.add(activeTraceInfo);
            }
        }
        return collectData;
    }

}
