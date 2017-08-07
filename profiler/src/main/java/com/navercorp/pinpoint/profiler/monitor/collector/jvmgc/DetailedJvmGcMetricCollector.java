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

import com.navercorp.pinpoint.profiler.monitor.metric.gc.DetailedGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.DetailedGarbageCollectorMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.DetailedMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.DetailedMemoryMetricSnapshot;
import com.navercorp.pinpoint.thrift.dto.TJvmGc;
import com.navercorp.pinpoint.thrift.dto.TJvmGcDetailed;

/**
 * @author dawidmalina
 * @author HyunGil Jeong
 */
public class DetailedJvmGcMetricCollector implements JvmGcMetricCollector {

    private final JvmGcMetricCollector jvmGcMetricCollector;
    private final DetailedMemoryMetric detailedMemoryMetric;
    private final DetailedGarbageCollectorMetric detailedGarbageCollectorMetric;

    public DetailedJvmGcMetricCollector(
            JvmGcMetricCollector jvmGcMetricCollector,
            DetailedMemoryMetric detailedMemoryMetric,
            DetailedGarbageCollectorMetric detailedGarbageCollectorMetric) {
        if (jvmGcMetricCollector == null) {
            throw new NullPointerException("jvmGcMetricCollector must not be null");
        }
        if (detailedMemoryMetric == null) {
            throw new NullPointerException("detailedMemoryMetric must not be null");
        }
        if (detailedGarbageCollectorMetric == null) {
            throw new NullPointerException("detailedGarbageCollectorMetric must not be null");
        }
        this.jvmGcMetricCollector = jvmGcMetricCollector;
        this.detailedMemoryMetric = detailedMemoryMetric;
        this.detailedGarbageCollectorMetric = detailedGarbageCollectorMetric;
    }

    @Override
    public TJvmGc collect() {
        TJvmGc jvmGc = jvmGcMetricCollector.collect();
        DetailedMemoryMetricSnapshot detailedMemoryMetricSnapshot = detailedMemoryMetric.getSnapshot();
        DetailedGarbageCollectorMetricSnapshot detailedGarbageCollectorMetricSnapshot = detailedGarbageCollectorMetric.getSnapshot();
        TJvmGcDetailed jvmGcDetailed = new TJvmGcDetailed();
        jvmGcDetailed.setJvmPoolNewGenUsed(detailedMemoryMetricSnapshot.getNewGenUsage());
        jvmGcDetailed.setJvmPoolOldGenUsed(detailedMemoryMetricSnapshot.getOldGenUsage());
        jvmGcDetailed.setJvmPoolSurvivorSpaceUsed(detailedMemoryMetricSnapshot.getSurvivorSpaceUsage());
        jvmGcDetailed.setJvmPoolCodeCacheUsed(detailedMemoryMetricSnapshot.getCodeCacheUsage());
        jvmGcDetailed.setJvmPoolPermGenUsed(detailedMemoryMetricSnapshot.getPermGenUsage());
        jvmGcDetailed.setJvmPoolMetaspaceUsed(detailedMemoryMetricSnapshot.getMetaspaceUsage());
        jvmGcDetailed.setJvmGcNewCount(detailedGarbageCollectorMetricSnapshot.getGcNewCount());
        jvmGcDetailed.setJvmGcNewTime(detailedGarbageCollectorMetricSnapshot.getGcNewTime());
        jvmGc.setJvmGcDetailed(jvmGcDetailed);
        return jvmGc;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DetailedJvmGcMetricCollector{");
        sb.append("jvmGcMetricCollector=").append(jvmGcMetricCollector);
        sb.append(", detailedMemoryMetric=").append(detailedMemoryMetric);
        sb.append(", detailedGarbageCollectorMetric=").append(detailedGarbageCollectorMetric);
        sb.append('}');
        return sb.toString();
    }
}
