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
import com.navercorp.pinpoint.profiler.monitor.metric.JvmGcDetailedMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.JvmGcMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.DetailedGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.DetailedGarbageCollectorMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.DetailedMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.DetailedMemoryMetricSnapshot;

/**
 * @author dawidmalina
 * @author HyunGil Jeong
 */
public class DetailedJvmGcMetricCollector implements AgentStatMetricCollector<JvmGcMetricSnapshot> {

    private final BasicJvmGcMetricCollector jvmGcMetricCollector;
    private final DetailedMemoryMetric detailedMemoryMetric;
    private final DetailedGarbageCollectorMetric detailedGarbageCollectorMetric;

    public DetailedJvmGcMetricCollector(
            BasicJvmGcMetricCollector jvmGcMetricCollector,
            DetailedMemoryMetric detailedMemoryMetric,
            DetailedGarbageCollectorMetric detailedGarbageCollectorMetric) {
        this.jvmGcMetricCollector = Assert.requireNonNull(jvmGcMetricCollector, "jvmGcMetricCollector");
        this.detailedMemoryMetric = Assert.requireNonNull(detailedMemoryMetric, "detailedMemoryMetric");
        this.detailedGarbageCollectorMetric = Assert.requireNonNull(detailedGarbageCollectorMetric, "detailedGarbageCollectorMetric");
    }


    @Override
    public JvmGcMetricSnapshot collect() {
        final JvmGcMetricSnapshot jvmGcMetricSnapshot = jvmGcMetricCollector.collect();
        final DetailedMemoryMetricSnapshot detailedMemoryMetricSnapshot = detailedMemoryMetric.getSnapshot();
        final DetailedGarbageCollectorMetricSnapshot detailedGarbageCollectorMetricSnapshot = detailedGarbageCollectorMetric.getSnapshot();

        final JvmGcDetailedMetricSnapshot jvmGcDetailedMetricSnapshot = new JvmGcDetailedMetricSnapshot();
        jvmGcDetailedMetricSnapshot.setJvmPoolNewGenUsed(detailedMemoryMetricSnapshot.getNewGenUsage());
        jvmGcDetailedMetricSnapshot.setJvmPoolOldGenUsed(detailedMemoryMetricSnapshot.getOldGenUsage());
        jvmGcDetailedMetricSnapshot.setJvmPoolSurvivorSpaceUsed(detailedMemoryMetricSnapshot.getSurvivorSpaceUsage());
        jvmGcDetailedMetricSnapshot.setJvmPoolCodeCacheUsed(detailedMemoryMetricSnapshot.getCodeCacheUsage());
        jvmGcDetailedMetricSnapshot.setJvmPoolPermGenUsed(detailedMemoryMetricSnapshot.getPermGenUsage());
        jvmGcDetailedMetricSnapshot.setJvmPoolMetaspaceUsed(detailedMemoryMetricSnapshot.getMetaspaceUsage());
        jvmGcDetailedMetricSnapshot.setJvmGcNewCount(detailedGarbageCollectorMetricSnapshot.getGcNewCount());
        jvmGcDetailedMetricSnapshot.setJvmGcNewTime(detailedGarbageCollectorMetricSnapshot.getGcNewTime());
        jvmGcMetricSnapshot.setJvmGcDetailed(jvmGcDetailedMetricSnapshot);
        return jvmGcMetricSnapshot;
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
