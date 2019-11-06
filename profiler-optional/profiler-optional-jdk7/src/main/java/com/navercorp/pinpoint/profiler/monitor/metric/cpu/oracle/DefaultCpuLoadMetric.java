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

package com.navercorp.pinpoint.profiler.monitor.metric.cpu.oracle;

import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetricSnapshot;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuUsageProvider;
import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

/**
 * @author HyunGil Jeong
 */
public class DefaultCpuLoadMetric implements CpuLoadMetric {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CpuUsageProvider jvmCpuUsageProvider;
    private final CpuUsageProvider systemCpuUsageProvider;

    public DefaultCpuLoadMetric() {
        final OperatingSystemMXBean operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        if (operatingSystemMXBean == null) {
            throw new IllegalStateException("OperatingSystemMXBean not available");
        }

        CpuUsageProvider jvmCpuUsageProvider = new JvmCpuUsageProvider(operatingSystemMXBean);
        try {
            jvmCpuUsageProvider.getCpuUsage();
        } catch (NoSuchMethodError e) {
            logger.warn("Expected method not found for retrieving jvm cpu usage. Cause : {}", e.getMessage());
            jvmCpuUsageProvider = CpuUsageProvider.UNSUPPORTED;
        }
        this.jvmCpuUsageProvider = jvmCpuUsageProvider;

        CpuUsageProvider systemCpuUsageProvider = new SystemCpuUsageProvider(operatingSystemMXBean);
        try {
            systemCpuUsageProvider.getCpuUsage();
        } catch (NoSuchMethodError e) {
            logger.warn("Expected method not found for retrieving system cpu usage. Cause : {}", e.getMessage());
            systemCpuUsageProvider = CpuUsageProvider.UNSUPPORTED;
        }
        this.systemCpuUsageProvider = systemCpuUsageProvider;
    }

    @Override
    public CpuLoadMetricSnapshot getSnapshot() {
        double jvmCpuUsage = jvmCpuUsageProvider.getCpuUsage();
        double systemCpuUsage = systemCpuUsageProvider.getCpuUsage();
        return new CpuLoadMetricSnapshot(jvmCpuUsage, systemCpuUsage);
    }

    @Override
    public String toString() {
        return "CpuLoadMetric for Oracle Java 1.7+";
    }

    private static class JvmCpuUsageProvider implements CpuUsageProvider {

        private final OperatingSystemMXBean operatingSystemMXBean;

        private JvmCpuUsageProvider(OperatingSystemMXBean operatingSystemMXBean) {
            this.operatingSystemMXBean = operatingSystemMXBean;
        }

        @Override
        public double getCpuUsage() {
            return operatingSystemMXBean.getProcessCpuLoad();
        }
    }

    private static class SystemCpuUsageProvider implements CpuUsageProvider {

        private final OperatingSystemMXBean operatingSystemMXBean;

        private SystemCpuUsageProvider(OperatingSystemMXBean operatingSystemMXBean) {
            this.operatingSystemMXBean = operatingSystemMXBean;
        }

        @Override
        public double getCpuUsage() {
            return operatingSystemMXBean.getSystemCpuLoad();
        }
    }
}
