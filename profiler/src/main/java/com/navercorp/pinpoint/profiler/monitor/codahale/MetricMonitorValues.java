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

import java.util.Map;
import java.util.SortedMap;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author harebox
 * @author hyungil.jeong
 */
public final class MetricMonitorValues {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricMonitorValues.class);

    public static final String JVM_GC = "jvm.gc";
    // Serial collector
    public static final String JVM_GC_SERIAL_OLDGEN_COUNT = JVM_GC + ".MarkSweepCompact.count";
    public static final String JVM_GC_SERIAL_OLDGEN_TIME = JVM_GC + ".MarkSweepCompact.time";
    public static final String JVM_GC_SERIAL_NEWGEN_COUNT = JVM_GC + ".Copy.count";
    public static final String JVM_GC_SERIAL_NEWGEN_TIME = JVM_GC + ".Copy.time";
    // Parallel (Old) collector
    public static final String JVM_GC_PS_OLDGEN_COUNT = JVM_GC + ".PS-MarkSweep.count";
    public static final String JVM_GC_PS_OLDGEN_TIME = JVM_GC + ".PS-MarkSweep.time";
    public static final String JVM_GC_PS_NEWGEN_COUNT = JVM_GC + ".PS-Scavenge.count";
    public static final String JVM_GC_PS_NEWGEN_TIME = JVM_GC + ".PS-Scavenge.time";
    // CMS collector
    public static final String JVM_GC_CMS_OLDGEN_COUNT = JVM_GC + ".ConcurrentMarkSweep.count";
    public static final String JVM_GC_CMS_OLDGEN_TIME = JVM_GC + ".ConcurrentMarkSweep.time";
    public static final String JVM_GC_CMS_NEWGEN_COUNT = JVM_GC + ".ParNew.count";
    public static final String JVM_GC_CMS_NEWGEN_TIME = JVM_GC + ".ParNew.time";
    // G1 collector
    public static final String JVM_GC_G1_OLDGEN_COUNT = JVM_GC + ".G1-Old-Generation.count";
    public static final String JVM_GC_G1_OLDGEN_TIME = JVM_GC + ".G1-Old-Generation.time";
    public static final String JVM_GC_G1_NEWGEN_COUNT = JVM_GC + ".G1-Young-Generation.count";
    public static final String JVM_GC_G1_NEWGEN_TIME = JVM_GC + ".G1-Young-Generation.time";

    public static final String JVM_MEMORY = "jvm.memory";
    // commons
    public static final String JVM_MEMORY_HEAP_INIT = JVM_MEMORY + ".heap.init";
    public static final String JVM_MEMORY_HEAP_USED = JVM_MEMORY + ".heap.used";
    public static final String JVM_MEMORY_HEAP_COMMITTED = JVM_MEMORY + ".heap.committed";
    public static final String JVM_MEMORY_HEAP_MAX = JVM_MEMORY + ".heap.max";
    public static final String JVM_MEMORY_NONHEAP_INIT = JVM_MEMORY + ".non-heap.init";
    public static final String JVM_MEMORY_NONHEAP_USED = JVM_MEMORY + ".non-heap.used";
    public static final String JVM_MEMORY_NONHEAP_COMMITTED = JVM_MEMORY + ".non-heap.committed";
    public static final String JVM_MEMORY_NONHEAP_MAX = JVM_MEMORY + ".non-heap.max";
    public static final String JVM_MEMORY_TOTAL_INIT = JVM_MEMORY + ".total.init";
    public static final String JVM_MEMORY_TOTAL_USED = JVM_MEMORY + ".total.used";
    public static final String JVM_MEMORY_TOTAL_COMMITTED = JVM_MEMORY + ".total.committed";
    public static final String JVM_MEMORY_TOTAL_MAX = JVM_MEMORY + ".total.max";
    // Serial collector ( -XX:+UseSerialGC )
    public static final String JVM_MEMORY_POOLS_SERIAL_CODE_CACHE_USAGE = JVM_MEMORY + ".pools.Code-Cache.usage";
    public static final String JVM_MEMORY_POOLS_SERIAL_NEWGEN_USAGE = JVM_MEMORY + ".pools.Eden-Space.usage";
    public static final String JVM_MEMORY_POOLS_SERIAL_OLDGEN_USAGE = JVM_MEMORY + ".pools.Tenured-Gen.usage";
    public static final String JVM_MEMORY_POOLS_SERIAL_SURVIVOR_USAGE = JVM_MEMORY + ".pools.Survivor-Space.usage";
    public static final String JVM_MEMORY_POOLS_SERIAL_PERMGEN_USAGE = JVM_MEMORY + ".pools.Perm-Gen.usage";
    public static final String JVM_MEMORY_POOLS_SERIAL_METASPACE_USAGE = JVM_MEMORY + ".pools.Metaspace.usage";
    // Parallel (Old) collector ( -XX:+UseParallelOldGC )
    public static final String JVM_MEMORY_POOLS_PS_CODE_CACHE_USAGE = JVM_MEMORY + ".pools.Code-Cache.usage";
    public static final String JVM_MEMORY_POOLS_PS_NEWGEN_USAGE = JVM_MEMORY + ".pools.PS-Eden-Space.usage";
    public static final String JVM_MEMORY_POOLS_PS_OLDGEN_USAGE = JVM_MEMORY + ".pools.PS-Old-Gen.usage";
    public static final String JVM_MEMORY_POOLS_PS_SURVIVOR_USAGE = JVM_MEMORY + ".pools.PS-Survivor-Space.usage";
    public static final String JVM_MEMORY_POOLS_PS_PERMGEN_USAGE = JVM_MEMORY + ".pools.PS-Perm-Gen.usage";
    public static final String JVM_MEMORY_POOLS_PS_METASPACE_USAGE = JVM_MEMORY + ".pools.Metaspace.usage";
    // CMS collector ( -XX:+UseConcMarkSweepGC )
    public static final String JVM_MEMORY_POOLS_CMS_CODE_CACHE_USAGE = JVM_MEMORY + ".pools.Code-Cache.usage";
    public static final String JVM_MEMORY_POOLS_CMS_NEWGEN_USAGE = JVM_MEMORY + ".pools.Par-Eden-Space.usage";
    public static final String JVM_MEMORY_POOLS_CMS_OLDGEN_USAGE = JVM_MEMORY + ".pools.CMS-Old-Gen.usage";
    public static final String JVM_MEMORY_POOLS_CMS_SURVIVOR_USAGE = JVM_MEMORY + ".pools.Par-Survivor-Space.usage";
    public static final String JVM_MEMORY_POOLS_CMS_PERMGEN_USAGE = JVM_MEMORY + ".pools.CMS-Perm-Gen.usage";
    public static final String JVM_MEMORY_POOLS_CMS_METASPACE_USAGE = JVM_MEMORY + ".pools.Metaspace.usage";
    // G1 collector ( -XX:+UseG1GC )
    public static final String JVM_MEMORY_POOLS_G1_CODE_CACHE_USAGE = JVM_MEMORY + ".pools.Code-Cache.usage";
    public static final String JVM_MEMORY_POOLS_G1_NEWGEN_USAGE = JVM_MEMORY + ".pools.G1-Eden-Space.usage";
    public static final String JVM_MEMORY_POOLS_G1_OLDGEN_USAGE = JVM_MEMORY + ".pools.G1-Old-Gen.usage";
    public static final String JVM_MEMORY_POOLS_G1_SURVIVOR_USAGE = JVM_MEMORY + ".pools.G1-Survivor-Space.usage";
    public static final String JVM_MEMORY_POOLS_G1_PERMGEN_USAGE = JVM_MEMORY + ".pools.G1-Perm-Gen.usage";
    public static final String JVM_MEMORY_POOLS_G1_METASPACE_USAGE = JVM_MEMORY + ".pools.Metaspace.usage";

    public static final String CPU_LOAD = "cpu.load";
    // CPU Load (JVM)
    public static final String CPU_LOAD_JVM = CPU_LOAD + ".jvm";
    // CPU Load (System)
    public static final String CPU_LOAD_SYSTEM = CPU_LOAD + ".system";

    public static final String TRANSACTION = "transaction";
    public static final String TRANSACTION_SAMPLED_NEW = TRANSACTION + ".sampled.new";
    public static final String TRANSACTION_SAMPLED_CONTINUATION = TRANSACTION + ".sampled.continuation";
    public static final String TRANSACTION_UNSAMPLED_NEW = TRANSACTION + ".unsampled.new";
    public static final String TRANSACTION_UNSAMPLED_CONTINUATION = TRANSACTION + ".unsampled.continuation";

    public static final String ACTIVE_TRACE = "active.trace";
    public static final String ACTIVE_TRACE_COUNT = ACTIVE_TRACE + ".count";

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
    public static <T> Gauge<T> getGauge(final SortedMap<String, Gauge<?>> gauges, final String key, final Gauge<T> defaultGauge) {
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
    public static Gauge<Long> getLongGauge(final SortedMap<String, Gauge> gauges, String key) {
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
    public static Gauge<Double> getDoubleGauge(final SortedMap<String, Gauge> gauges, String key) {
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
