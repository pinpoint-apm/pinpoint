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

package com.navercorp.pinpoint.profiler.context.provider.stat.cpu;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.monitor.collector.cpu.DefaultCpuLoadMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.cpu.CpuLoadMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.cpu.UnsupportedCpuLoadMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;

/**
 * @author HyunGil Jeong
 */
public class CpuLoadMetricCollectorProvider implements Provider<CpuLoadMetricCollector> {

    private final CpuLoadMetric cpuLoadMetric;

    @Inject
    public CpuLoadMetricCollectorProvider(CpuLoadMetric cpuLoadMetric) {
        if (cpuLoadMetric == null) {
            throw new NullPointerException("cpuLoadMetric must not be null");
        }
        this.cpuLoadMetric = cpuLoadMetric;
    }

    @Override
    public CpuLoadMetricCollector get() {
        if (cpuLoadMetric == CpuLoadMetric.UNSUPPORTED_CPU_LOAD_METRIC) {
            return new UnsupportedCpuLoadMetricCollector();
        }
        return new DefaultCpuLoadMetricCollector(cpuLoadMetric);
    }
}
