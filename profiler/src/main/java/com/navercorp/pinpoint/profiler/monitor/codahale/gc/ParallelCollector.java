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

package com.navercorp.pinpoint.profiler.monitor.codahale.gc;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorRegistry;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TJvmGcType;

import java.util.SortedMap;

import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

/**
 * HotSpot's Parallel (Old) collector
 *
 * @author emeroad
 * @author harebox
 * @author dawidmalina
 */
public class ParallelCollector implements GarbageCollector {

    public static final TJvmGcType GC_TYPE = TJvmGcType.PARALLEL;

    private final Gauge<Long> heapMax;
    private final Gauge<Long> heapUsed;

    private final Gauge<Long> heapNonHeapMax;
    private final Gauge<Long> heapNonHeapUsed;

    private final Gauge<Long> oldGcCount;
    private final Gauge<Long> oldGcTime;

    public ParallelCollector(MetricMonitorRegistry registry) {

        if (registry == null) {
            throw new NullPointerException("registry must not be null");
        }

        final MetricRegistry metricRegistry = registry.getRegistry();
        @SuppressWarnings("rawtypes")
        final SortedMap<String, Gauge> gauges = metricRegistry.getGauges();

        this.heapMax = getLongGauge(gauges, JVM_MEMORY_HEAP_MAX);
        this.heapUsed = getLongGauge(gauges, JVM_MEMORY_HEAP_USED);

        this.heapNonHeapMax = getLongGauge(gauges, JVM_MEMORY_NONHEAP_MAX);
        this.heapNonHeapUsed = getLongGauge(gauges, JVM_MEMORY_NONHEAP_USED);

        this.oldGcCount = getLongGauge(gauges, JVM_GC_PS_OLDGEN_COUNT);
        this.oldGcTime = getLongGauge(gauges, JVM_GC_PS_OLDGEN_TIME);

    }

    @Override
    public int getTypeCode() {
        return GC_TYPE.getValue();
    }

    @Override
    public TJvmGc collect() {

        final TJvmGc gc = new TJvmGc();
        gc.setType(GC_TYPE);
        gc.setJvmMemoryHeapMax(heapMax.getValue());
        gc.setJvmMemoryHeapUsed(heapUsed.getValue());
        gc.setJvmMemoryNonHeapMax(heapNonHeapMax.getValue());
        gc.setJvmMemoryNonHeapUsed(heapNonHeapUsed.getValue());
        gc.setJvmGcOldCount(oldGcCount.getValue());
        gc.setJvmGcOldTime(oldGcTime.getValue());

        return gc;
    }

    @Override
    public String toString() {
        return "HotSpot's Parallel (Old) collector";
    }

}
