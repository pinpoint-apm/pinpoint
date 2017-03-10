/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.collector.jvmgc;

import com.navercorp.pinpoint.profiler.monitor.metric.gc.GarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.MemoryMetric;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;

/**
 * @author HyunGil Jeong
 */
public class JvmGcCommonMetricCollector implements JvmGcMetricCollector {

    private final MemoryMetric memoryMetric;

    private final GarbageCollectorMetric garbageCollectorMetric;

    public JvmGcCommonMetricCollector(MemoryMetric memoryMetric, GarbageCollectorMetric garbageCollectorMetric) {
        if (memoryMetric == null) {
            throw new NullPointerException("memoryMetric must not be null");
        }
        if (garbageCollectorMetric == null) {
            throw new NullPointerException("garbageCollectorMetric must not be null");
        }
        this.memoryMetric = memoryMetric;
        this.garbageCollectorMetric = garbageCollectorMetric;
    }

    @Override
    public TJvmGc collect() {
        TJvmGc jvmGc = new TJvmGc();
        jvmGc.setJvmMemoryHeapMax(memoryMetric.heapMax());
        jvmGc.setJvmMemoryHeapUsed(memoryMetric.heapUsed());
        jvmGc.setJvmMemoryNonHeapMax(memoryMetric.nonHeapMax());
        jvmGc.setJvmMemoryNonHeapUsed(memoryMetric.nonHeapUsed());
        jvmGc.setJvmGcOldCount(garbageCollectorMetric.gcOldCount());
        jvmGc.setJvmGcOldTime(garbageCollectorMetric.gcOldTime());
        jvmGc.setType(garbageCollectorMetric.gcType());
        return jvmGc;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JvmGcCommonMetricCollector{");
        sb.append("memoryMetric=").append(memoryMetric);
        sb.append(", garbageCollectorMetric=").append(garbageCollectorMetric);
        sb.append('}');
        return sb.toString();
    }
}
