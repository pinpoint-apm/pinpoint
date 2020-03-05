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
import com.navercorp.pinpoint.common.trace.BaseHistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    // oom safe cache
    private final ConcurrentMap<ActiveTraceHandle, ActiveTrace> activeTraceInfoMap;

    private final ResponseTimeCollector responseTimeCollector;

    private final HistogramSchema histogramSchema = BaseHistogramSchema.NORMAL_SCHEMA;
    private final ActiveTraceHistogram emptyActiveTraceHistogram = new EmptyActiveTraceHistogram(histogramSchema);

    public DefaultActiveTraceRepository(ResponseTimeCollector responseTimeCollector) {
        this(responseTimeCollector, DEFAULT_MAX_ACTIVE_TRACE_SIZE);
    }

    public DefaultActiveTraceRepository(ResponseTimeCollector responseTimeCollector, int maxActiveTraceSize) {
        this.responseTimeCollector = Assert.requireNonNull(responseTimeCollector, "responseTimeCollector");
        this.activeTraceInfoMap = createCache(maxActiveTraceSize);
    }

    private ConcurrentMap<ActiveTraceHandle, ActiveTrace> createCache(int maxActiveTraceSize) {
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        cacheBuilder.concurrencyLevel(64);
        cacheBuilder.initialCapacity(maxActiveTraceSize);
        cacheBuilder.maximumSize(maxActiveTraceSize);

        final Cache<ActiveTraceHandle, ActiveTrace> localCache = cacheBuilder.build();
        return localCache.asMap();
    }


    private void remove(ActiveTraceHandle key, long purgeTime) {
        if (isDebug) {
            logger.debug("remove ActiveTrace key:{}", key);
        }
        final ActiveTrace activeTrace = this.activeTraceInfoMap.remove(key);
        if (activeTrace != null) {
            final long responseTime = purgeTime - activeTrace.getStartTime();
            responseTimeCollector.add(responseTime);
        }
    }

    @Override
    public ActiveTraceHandle register(TraceRoot traceRoot) {
        final ActiveTrace activeTrace = newSampledActiveTrace(traceRoot);
        return register0(activeTrace);
    }

    private ActiveTrace newSampledActiveTrace(TraceRoot traceRoot) {
        return new SampledActiveTrace(traceRoot);
    }

    @Override
    public ActiveTraceHandle register(long localTransactionId, long startTime, long threadId) {
        final ActiveTrace activeTrace = newUnsampledActiveTrace(localTransactionId, startTime, threadId);
        return register0(activeTrace);
    }

    private ActiveTrace newUnsampledActiveTrace(long localTransactionId, long startTime, long threadId) {
        return new UnsampledActiveTrace(localTransactionId, startTime, threadId);
    }

    private ActiveTraceHandle register0(ActiveTrace activeTrace) {
        if (isDebug) {
            logger.debug("register ActiveTrace key:{}", activeTrace);
        }

        final long id = activeTrace.getId();
        final ActiveTraceHandle handle = new DefaultActiveTraceHandle(id);
        final ActiveTrace old = this.activeTraceInfoMap.put(handle, activeTrace);
        if (old != null) {
            if (logger.isWarnEnabled()) {
                logger.warn("old activeTrace exist:{}", old);
            }
        }
        return handle;
    }


    // @ThreadSafe
    @Override
    public List<ActiveTraceSnapshot> snapshot() {
        if (this.activeTraceInfoMap.isEmpty()) {
            return Collections.emptyList();
        }
        final Collection<ActiveTrace> activeTraceCollection = this.activeTraceInfoMap.values();
        final List<ActiveTraceSnapshot> collectData = new ArrayList<ActiveTraceSnapshot>(activeTraceCollection.size());

        for (ActiveTrace trace : activeTraceCollection) {
            final long startTime = trace.getStartTime();
            // not started
            if (!isStarted(startTime)) {
                continue;
            }
            final ActiveTraceSnapshot snapshot = trace.snapshot();
            collectData.add(snapshot);
        }
        if (isDebug) {
            logger.debug("activeTraceSnapshot size:{}", collectData.size());
        }
        return collectData;
    }


    // @ThreadSafe
    @Override
    public List<Long> getThreadIdList() {
        if (this.activeTraceInfoMap.isEmpty()) {
            return Collections.emptyList();
        }
        final Collection<ActiveTrace> activeTraceCollection = this.activeTraceInfoMap.values();
        final List<Long> collectData = new ArrayList<Long>(activeTraceCollection.size());

        for (ActiveTrace trace : activeTraceCollection) {
            final long startTime = trace.getStartTime();
            // not started
            if (!isStarted(startTime)) {
                continue;
            }
            final ActiveTraceSnapshot snapshot = trace.snapshot();
            collectData.add(snapshot.getThreadId());
        }
        if (isDebug) {
            logger.debug("activeTraceSnapshot size:{}", collectData.size());
        }
        return collectData;
    }

    // @ThreadSafe
    @Override
    public ActiveTraceHistogram getActiveTraceHistogram(long currentTime) {
        if (this.activeTraceInfoMap.isEmpty()) {
            return emptyActiveTraceHistogram;
        }
        final Collection<ActiveTrace> activeTraceCollection = this.activeTraceInfoMap.values();


        final DefaultActiveTraceHistogram histogram = new DefaultActiveTraceHistogram(histogramSchema);
        for (ActiveTrace activeTraceInfo : activeTraceCollection) {
            final long startTime = activeTraceInfo.getStartTime();
            if (!isStarted(startTime)) {
                continue;
            }
            final int elapsedTime = (int) (currentTime - startTime);
            final HistogramSlot slot = histogramSchema.findHistogramSlot(elapsedTime, false);
            histogram.increment(slot);
        }

        return histogram;
    }

    private boolean isStarted(long startTime) {
        return startTime > 0;
    }


    private class DefaultActiveTraceHandle implements ActiveTraceHandle {
        private final long id;

        DefaultActiveTraceHandle(long id) {
            this.id = id;
        }

        @Override
        public void purge(long purgeTime) {
            remove(this, purgeTime);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DefaultActiveTraceHandle that = (DefaultActiveTraceHandle) o;

            return id == that.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }

        @Override
        public String toString() {
            return "DefaultActiveTraceHandle{" +
                    "id=" + id +
                    '}';
        }
    }

}
