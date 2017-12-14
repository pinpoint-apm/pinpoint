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

package com.navercorp.pinpoint.profiler.monitor.collector.cpu;

import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetricSnapshot;
import com.navercorp.pinpoint.thrift.dto.TCpuLoad;

/**
 * @author HyunGil Jeong
 */
public class DefaultCpuLoadMetricCollector implements CpuLoadMetricCollector {

    private final CpuLoadMetric cpuLoadMetric;

    public DefaultCpuLoadMetricCollector(CpuLoadMetric cpuLoadMetric) {
        if (cpuLoadMetric == null) {
            throw new NullPointerException("cpuLoadMetric must not be null");
        }
        this.cpuLoadMetric = cpuLoadMetric;
    }

    @Override
    public TCpuLoad collect() {
        TCpuLoad cpuLoad = new TCpuLoad();
        CpuLoadMetricSnapshot snapshot = cpuLoadMetric.getSnapshot();
        cpuLoad.setJvmCpuLoad(snapshot.getJvmCpuUsage());
        cpuLoad.setSystemCpuLoad(snapshot.getSystemCpuUsage());
        return cpuLoad;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultCpuLoadMetricCollector{");
        sb.append("cpuLoadMetric=").append(cpuLoadMetric);
        sb.append('}');
        return sb.toString();
    }
}
