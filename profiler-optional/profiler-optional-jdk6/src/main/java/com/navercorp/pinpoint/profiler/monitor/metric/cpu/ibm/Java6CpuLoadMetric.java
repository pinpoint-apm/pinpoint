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

import com.ibm.lang.management.OperatingSystemMXBean;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuUsageProvider;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.JvmCpuUsageCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author HyunGil Jeong
 */
public class Java6CpuLoadMetric implements CpuLoadMetric {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private CpuUsageProvider jvmCpuUsageProvider;

    public Java6CpuLoadMetric() {
        final OperatingSystemMXBean operatingSystemMXBean = (com.ibm.lang.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        if (operatingSystemMXBean == null) {
            throw new IllegalStateException("OperatingSystemMXBean not available");
        }
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        if (runtimeMXBean == null) {
            throw new IllegalStateException("RuntimeMXBean not available");
        }

        CpuUsageProvider jvmCpuUsageProvider = new JvmCpuUsageProvider(operatingSystemMXBean, runtimeMXBean);
        try {
            jvmCpuUsageProvider.getCpuUsage();
        } catch (NoSuchMethodError e) {
            logger.warn("Expected method not found for retrieving jvm cpu usage. Cause : {}", e.getMessage());
            jvmCpuUsageProvider = CpuUsageProvider.UNSUPPORTED;
        }
        this.jvmCpuUsageProvider = jvmCpuUsageProvider;
    }

    @Override
    public CpuLoadMetricSnapshot getSnapshot() {
        double jvmCpuUsage = jvmCpuUsageProvider.getCpuUsage();
        return new CpuLoadMetricSnapshot(jvmCpuUsage, UNCOLLECTED_USAGE);
    }

    @Override
    public String toString() {
        return "CpuLoadMetric for IBM Java 1.6";
    }

    private static class JvmCpuUsageProvider implements CpuUsageProvider {

        private final JvmCpuUsageCalculator jvmCpuUsageCalculator = new JvmCpuUsageCalculator();

        private final OperatingSystemMXBean operatingSystemMXBean;
        private final RuntimeMXBean runtimeMXBean;

        private JvmCpuUsageProvider(OperatingSystemMXBean operatingSystemMXBean, RuntimeMXBean runtimeMXBean) {
            this.operatingSystemMXBean = operatingSystemMXBean;
            this.runtimeMXBean = runtimeMXBean;
        }

        @Override
        public double getCpuUsage() {
            long cpuTimeNS = operatingSystemMXBean.getProcessCpuTimeByNS();
            long upTimeMS = runtimeMXBean.getUptime();
            return jvmCpuUsageCalculator.getJvmCpuUsage(cpuTimeNS, upTimeMS);
        }
    }
}
