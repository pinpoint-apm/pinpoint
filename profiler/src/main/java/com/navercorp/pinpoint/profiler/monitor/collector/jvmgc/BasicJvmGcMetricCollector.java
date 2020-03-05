/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.collector.jvmgc;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.JvmGcMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.GarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.GarbageCollectorMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.JvmGcType;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.MemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.MemoryMetricSnapshot;

/**
 * @author HyunGil Jeong
 */
public class BasicJvmGcMetricCollector implements AgentStatMetricCollector<JvmGcMetricSnapshot> {

    private final MemoryMetric memoryMetric;
    private final GarbageCollectorMetric garbageCollectorMetric;

    public BasicJvmGcMetricCollector(MemoryMetric memoryMetric, GarbageCollectorMetric garbageCollectorMetric) {
        this.memoryMetric = Assert.requireNonNull(memoryMetric, "memoryMetric");
        this.garbageCollectorMetric = Assert.requireNonNull(garbageCollectorMetric, "garbageCollectorMetric");
    }

    @Override
    public JvmGcMetricSnapshot collect() {
        final JvmGcType jvmGcType = garbageCollectorMetric.getGcType();
        final MemoryMetricSnapshot memoryMetricSnapshot = memoryMetric.getSnapshot();
        final GarbageCollectorMetricSnapshot garbageCollectorMetricSnapshot = garbageCollectorMetric.getSnapshot();

        final JvmGcMetricSnapshot jvmGcMetricSnapshot = new JvmGcMetricSnapshot();
        jvmGcMetricSnapshot.setJvmMemoryHeapMax(memoryMetricSnapshot.getHeapMax());
        jvmGcMetricSnapshot.setJvmMemoryHeapUsed(memoryMetricSnapshot.getHeapUsed());
        jvmGcMetricSnapshot.setJvmMemoryNonHeapMax(memoryMetricSnapshot.getNonHeapMax());
        jvmGcMetricSnapshot.setJvmMemoryNonHeapUsed(memoryMetricSnapshot.getNonHeapUsed());
        jvmGcMetricSnapshot.setJvmGcOldCount(garbageCollectorMetricSnapshot.getGcOldCount());
        jvmGcMetricSnapshot.setJvmGcOldTime(garbageCollectorMetricSnapshot.getGcOldTime());
        jvmGcMetricSnapshot.setType(jvmGcType);
        return jvmGcMetricSnapshot;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BasicJvmGcMetricCollector{");
        sb.append("memoryMetric=").append(memoryMetric);
        sb.append(", garbageCollectorMetric=").append(garbageCollectorMetric);
        sb.append('}');
        return sb.toString();
    }
}