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

package com.navercorp.pinpoint.profiler.monitor.metric.cpu.ibm;

import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.JvmCpuUsageCalculator;

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

/**
 * @author HyunGil Jeong
 */
public class Java6CpuLoadMetric implements CpuLoadMetric {

    private final JvmCpuUsageCalculator jvmCpuUsageCalculator = new JvmCpuUsageCalculator();
    private final com.ibm.lang.management.OperatingSystemMXBean operatingSystemMXBean;
    private final RuntimeMXBean runtimeMXBean;

    public Java6CpuLoadMetric(OperatingSystemMXBean operatingSystemMXBean, RuntimeMXBean runtimeMXBean) {
        if (operatingSystemMXBean == null) {
            throw new NullPointerException("operatingSystemMXBean must not be null");
        }
        this.operatingSystemMXBean = (com.ibm.lang.management.OperatingSystemMXBean) operatingSystemMXBean;
        this.runtimeMXBean = runtimeMXBean;
    }

    @Override
    public CpuLoadMetricSnapshot getSnapshot() {
        double jvmCpuUsage = UNCOLLECTED_USAGE;
        if (runtimeMXBean != null) {
            long cpuTimeNS = operatingSystemMXBean.getProcessCpuTimeByNS();
            long upTimeMS = runtimeMXBean.getUptime();
            jvmCpuUsage = jvmCpuUsageCalculator.getJvmCpuUsage(cpuTimeNS, upTimeMS);
        }
        double systemCpuUsage = operatingSystemMXBean.getSystemCpuLoad();
        return new CpuLoadMetricSnapshot(jvmCpuUsage, systemCpuUsage);
    }

    @Override
    public String toString() {
        return "CpuLoadMetric for IBM Java 1.6";
    }
}
