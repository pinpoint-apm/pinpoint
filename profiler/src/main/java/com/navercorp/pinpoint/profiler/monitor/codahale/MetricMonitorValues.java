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

package com.navercorp.pinpoint.profiler.monitor.codahale;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author emeroad
 * @author harebox
 * @author hyungil.jeong
 */
public final class MetricMonitorValues {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricMonitorValues.class);

    // Serial collector
    public static final String METRIC_GC_SERIAL_OLDGEN_COUNT = "MarkSweepCompact.count";
    public static final String METRIC_GC_SERIAL_OLDGEN_TIME = "MarkSweepCompact.time";
    public static final String METRIC_GC_SERIAL_NEWGEN_COUNT = "Copy.count";
    public static final String METRIC_GC_SERIAL_NEWGEN_TIME = "Copy.time";
    // Parallel (Old) collector
    public static final String METRIC_GC_PS_OLDGEN_COUNT = "PS-MarkSweep.count";
    public static final String METRIC_GC_PS_OLDGEN_TIME = "PS-MarkSweep.time";
    public static final String METRIC_GC_PS_NEWGEN_COUNT = "PS-Scavenge.count";
    public static final String METRIC_GC_PS_NEWGEN_TIME = "PS-Scavenge.time";
    // CMS collector
    public static final String METRIC_GC_CMS_OLDGEN_COUNT = "ConcurrentMarkSweep.count";
    public static final String METRIC_GC_CMS_OLDGEN_TIME = "ConcurrentMarkSweep.time";
    public static final String METRIC_GC_CMS_NEWGEN_COUNT = "ParNew.count";
    public static final String METRIC_GC_CMS_NEWGEN_TIME = "ParNew.time";
    // G1 collector
    public static final String METRIC_GC_G1_OLDGEN_COUNT = "G1-Old-Generation.count";
    public static final String METRIC_GC_G1_OLDGEN_TIME = "G1-Old-Generation.time";
    public static final String METRIC_GC_G1_NEWGEN_COUNT = "G1-Young-Generation.count";
    public static final String METRIC_GC_G1_NEWGEN_TIME = "G1-Young-Generation.time";

    // commons
    public static final String METRIC_MEMORY_HEAP_INIT = "heap.init";
    public static final String METRIC_MEMORY_HEAP_USED = "heap.used";
    public static final String METRIC_MEMORY_HEAP_COMMITTED = "heap.committed";
    public static final String METRIC_MEMORY_HEAP_MAX = "heap.max";
    public static final String METRIC_MEMORY_NONHEAP_INIT = "non-heap.init";
    public static final String METRIC_MEMORY_NONHEAP_USED = "non-heap.used";
    public static final String METRIC_MEMORY_NONHEAP_COMMITTED = "non-heap.committed";
    public static final String METRIC_MEMORY_NONHEAP_MAX = "non-heap.max";
    public static final String METRIC_MEMORY_TOTAL_INIT = "total.init";
    public static final String METRIC_MEMORY_TOTAL_USED = "total.used";
    public static final String METRIC_MEMORY_TOTAL_COMMITTED = "total.committed";
    public static final String METRIC_MEMORY_TOTAL_MAX = "total.max";
    // Serial collector ( -XX:+UseSerialGC )
    public static final String METRIC_MEMORY_POOLS_SERIAL_CODE_CACHE_USAGE = "pools.Code-Cache.usage";
    public static final String METRIC_MEMORY_POOLS_SERIAL_NEWGEN_USAGE = "pools.Eden-Space.usage";
    public static final String METRIC_MEMORY_POOLS_SERIAL_OLDGEN_USAGE = "pools.Tenured-Gen.usage";
    public static final String METRIC_MEMORY_POOLS_SERIAL_SURVIVOR_USAGE = "pools.Survivor-Space.usage";
    public static final String METRIC_MEMORY_POOLS_SERIAL_PERMGEN_USAGE = "pools.Perm-Gen.usage";
    public static final String METRIC_MEMORY_POOLS_SERIAL_METASPACE_USAGE = "pools.Metaspace.usage";
    // Parallel (Old) collector ( -XX:+UseParallelOldGC )
    public static final String METRIC_MEMORY_POOLS_PS_CODE_CACHE_USAGE = "pools.Code-Cache.usage";
    public static final String METRIC_MEMORY_POOLS_PS_NEWGEN_USAGE = "pools.PS-Eden-Space.usage";
    public static final String METRIC_MEMORY_POOLS_PS_OLDGEN_USAGE = "pools.PS-Old-Gen.usage";
    public static final String METRIC_MEMORY_POOLS_PS_SURVIVOR_USAGE = "pools.PS-Survivor-Space.usage";
    public static final String METRIC_MEMORY_POOLS_PS_PERMGEN_USAGE = "pools.PS-Perm-Gen.usage";
    public static final String METRIC_MEMORY_POOLS_PS_METASPACE_USAGE = "pools.Metaspace.usage";
    // CMS collector ( -XX:+UseConcMarkSweepGC )
    public static final String METRIC_MEMORY_POOLS_CMS_CODE_CACHE_USAGE = "pools.Code-Cache.usage";
    public static final String METRIC_MEMORY_POOLS_CMS_NEWGEN_USAGE = "pools.Par-Eden-Space.usage";
    public static final String METRIC_MEMORY_POOLS_CMS_OLDGEN_USAGE = "pools.CMS-Old-Gen.usage";
    public static final String METRIC_MEMORY_POOLS_CMS_SURVIVOR_USAGE = "pools.Par-Survivor-Space.usage";
    public static final String METRIC_MEMORY_POOLS_CMS_PERMGEN_USAGE = "pools.CMS-Perm-Gen.usage";
    public static final String METRIC_MEMORY_POOLS_CMS_METASPACE_USAGE = "pools.Metaspace.usage";
    // G1 collector ( -XX:+UseG1GC )
    public static final String METRIC_MEMORY_POOLS_G1_CODE_CACHE_USAGE = "pools.Code-Cache.usage";
    public static final String METRIC_MEMORY_POOLS_G1_NEWGEN_USAGE = "pools.G1-Eden-Space.usage";
    public static final String METRIC_MEMORY_POOLS_G1_OLDGEN_USAGE = "pools.G1-Old-Gen.usage";
    public static final String METRIC_MEMORY_POOLS_G1_SURVIVOR_USAGE = "pools.G1-Survivor-Space.usage";
    public static final String METRIC_MEMORY_POOLS_G1_PERMGEN_USAGE = "pools.G1-Perm-Gen.usage";
    public static final String METRIC_MEMORY_POOLS_G1_METASPACE_USAGE = "pools.Metaspace.usage";

    private MetricMonitorValues() {
    }

    public static long getLong(Gauge<Long> gauge) {
        if (gauge == null) {
            return 0;
        }
        return gauge.getValue();
    }

    public static <T> T getValue(Gauge<T> gauge, T defaultValue) {
        if (gauge == null) {
            return defaultValue;
        }
        return gauge.getValue();
    }

    public static <T extends Metric> T getMetric(final Map<String, T> metrics, final String key, final T defaultMetric) {
        if (metrics == null) {
            throw new NullPointerException("metrics must not be null");
        }
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        T metric = metrics.get(key);
        if (metric == null) {
            LOGGER.warn("key:{} not found", key);
            return defaultMetric;
        }
        return metric;
    }

    @SuppressWarnings("unchecked")
    public static <T> Gauge<T> getGauge(final Map<String, Gauge<?>> gauges, final String key, final Gauge<T> defaultGauge) {
        if (gauges == null) {
            throw new NullPointerException("gauges must not be null");
        }
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        Gauge<T> gauge = null;
        try {
            gauge = (Gauge<T>) gauges.get(key);
            if (gauge == null) {
                LOGGER.warn("key:{} not found", key);
                return defaultGauge;
            }
            return gauge;
        } catch (ClassCastException e) {
            LOGGER.warn("invalid gauge type. key:{} gauge:{}", key, gauge);
            return defaultGauge;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Gauge<Long> getLongGauge(final Map<String, Gauge> gauges, String key) {
        if (gauges == null) {
            throw new NullPointerException("gauges must not be null");
        }
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        final Gauge gauge = gauges.get(key);
        if (gauge == null) {
            LOGGER.warn("key:{} not found", key);
            return LONG_ZERO;
        }
        // Is there better way to check type of value?
        Object value = gauge.getValue();
        if (value instanceof Long) {
            return gauge;
        }
        LOGGER.warn("invalid gauge type. key:{} gauge:{}", key, gauge);
        return LONG_ZERO;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Gauge<Double> getDoubleGauge(final Map<String, Gauge> gauges, String key) {
        if (gauges == null) {
            throw new NullPointerException("gauges must not be null");
        }
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        final Gauge gauge = gauges.get(key);
        if (gauge == null) {
            LOGGER.warn("key:{} not found", key);
            return DOUBLE_ZERO;
        }
        // Is there better way to check type of value?
        Object value = gauge.getValue();
        if (value instanceof Double) {
            return gauge;
        }
        LOGGER.warn("invalid gauge type. key:{} gauge:{}", key, gauge);
        return DOUBLE_ZERO;
    }

    public static final Gauge<Long> LONG_ZERO = new EmptyGauge<Long>(0L);
    public static final Gauge<Long> EXCLUDED_LONG = new EmptyGauge<Long>(null);
    public static final Gauge<Double> DOUBLE_ZERO = new EmptyGauge<Double>(0D);
    public static final Gauge<Double> EXCLUDED_DOUBLE = new EmptyGauge<Double>(null);

    public static class EmptyGauge<T> implements Gauge<T> {
        private T emptyValue;

        public EmptyGauge(T emptyValue) {
            this.emptyValue = emptyValue;
        }

        @Override
        public T getValue() {
            return emptyValue;
        }
    }

}
