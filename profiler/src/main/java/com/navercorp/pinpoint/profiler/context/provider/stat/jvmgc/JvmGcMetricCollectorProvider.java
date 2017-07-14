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

package com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.monitor.collector.jvmgc.JvmGcDetailedMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.jvmgc.JvmGcCommonMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.jvmgc.JvmGcMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.GarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.MemoryMetric;


/**
 * @author HyunGil Jeong
 */
public class JvmGcMetricCollectorProvider implements Provider<JvmGcMetricCollector> {

    private final boolean collectDetailedMetrics;
    private final MemoryMetric memoryMetric;
    private final GarbageCollectorMetric garbageCollectorMetric;

    @Inject
    public JvmGcMetricCollectorProvider(ProfilerConfig profilerConfig, MemoryMetric memoryMetric, GarbageCollectorMetric garbageCollectorMetric) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (memoryMetric == null) {
            throw new NullPointerException("memoryMetric must not be null");
        }
        if (garbageCollectorMetric == null) {
            throw new NullPointerException("garbageCollectorMetric must not be null");
        }
        this.collectDetailedMetrics = profilerConfig.isProfilerJvmCollectDetailedMetrics();
        this.memoryMetric = memoryMetric;
        this.garbageCollectorMetric = garbageCollectorMetric;
    }

    @Override
    public JvmGcMetricCollector get() {
        JvmGcMetricCollector jvmGcMetricCollector;
        JvmGcCommonMetricCollector jvmGcCommonMetricCollector = new JvmGcCommonMetricCollector(memoryMetric, garbageCollectorMetric);
        if (collectDetailedMetrics) {
            jvmGcMetricCollector = new JvmGcDetailedMetricCollector(jvmGcCommonMetricCollector, memoryMetric, garbageCollectorMetric);
        } else {
            jvmGcMetricCollector = jvmGcCommonMetricCollector;
        }
        return jvmGcMetricCollector;
    }
}
