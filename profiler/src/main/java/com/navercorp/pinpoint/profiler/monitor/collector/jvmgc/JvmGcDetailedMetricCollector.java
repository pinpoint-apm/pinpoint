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
import com.navercorp.pinpoint.thrift.dto.TJvmGcDetailed;

/**
 * @author dawidmalina
 * @author HyunGil Jeong
 */
public class JvmGcDetailedMetricCollector implements JvmGcMetricCollector {

    private final JvmGcCommonMetricCollector jvmGcCommonMetricCollector;
    private final MemoryMetric memoryMetric;
    private final GarbageCollectorMetric garbageCollectorMetric;

    public JvmGcDetailedMetricCollector(JvmGcCommonMetricCollector jvmGcCommonMetricCollector, MemoryMetric memoryMetric, GarbageCollectorMetric garbageCollectorMetric) {
        this.jvmGcCommonMetricCollector = jvmGcCommonMetricCollector;
        this.memoryMetric = memoryMetric;
        this.garbageCollectorMetric = garbageCollectorMetric;
    }

    @Override
    public TJvmGc collect() {
        TJvmGcDetailed jvmGcDetailed = new TJvmGcDetailed();
        jvmGcDetailed.setJvmPoolNewGenUsed(memoryMetric.newGenUsage());
        jvmGcDetailed.setJvmPoolOldGenUsed(memoryMetric.oldGenUsage());
        jvmGcDetailed.setJvmPoolCodeCacheUsed(memoryMetric.codeCacheUsage());
        jvmGcDetailed.setJvmPoolSurvivorSpaceUsed(memoryMetric.survivorUsage());
        Double permGenUsed = memoryMetric.permGenUsage();
        if (permGenUsed != null) {
            // metric for jvm < 1.8
            jvmGcDetailed.setJvmPoolPermGenUsed(memoryMetric.permGenUsage());
        } else {
            // metric for jvm >= 1.8
            jvmGcDetailed.setJvmPoolMetaspaceUsed(memoryMetric.metaspaceUsage());
        }
        jvmGcDetailed.setJvmGcNewCount(garbageCollectorMetric.gcNewCount());
        jvmGcDetailed.setJvmGcNewTime(garbageCollectorMetric.gcNewTime());

        TJvmGc jvmGc = jvmGcCommonMetricCollector.collect();
        jvmGc.setJvmGcDetailed(jvmGcDetailed);
        return jvmGc;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JvmGcDetailedMetricCollector{");
        sb.append("memoryMetric=").append(memoryMetric);
        sb.append(", garbageCollectorMetric=").append(garbageCollectorMetric);
        sb.append('}');
        return sb.toString();
    }
}
