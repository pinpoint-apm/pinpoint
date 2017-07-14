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

import com.codahale.metrics.Gauge;
import com.navercorp.pinpoint.common.util.CpuUtils;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author HyunGil Jeong
 */
public class Java6CpuLoadMetric implements CpuLoadMetric {

    private static final int CPU_COUNT = CpuUtils.cpuCount();

    private static final int UNSUPPORTED = -1;
    private static final int UNINITIALIZED = -1;

    private final Gauge<Double> jvmCpuLoadGauge;
    private final Gauge<Double> systemCpuLoadGauge;

    public Java6CpuLoadMetric() {
        final OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        jvmCpuLoadGauge = createJvmCpuLoadGauge(operatingSystemMXBean, runtimeMXBean);
        systemCpuLoadGauge = UNSUPPORTED_GAUGE;
    }

    @Override
    public Double jvmCpuLoad() {
        return jvmCpuLoadGauge.getValue();
    }

    @Override
    public Double systemCpuLoad() {
        return systemCpuLoadGauge.getValue();
    }

    @Override
    public String toString() {
        return "CpuLoadMetric for Oracle Java 1.6";
    }

    private Gauge<Double> createJvmCpuLoadGauge(final OperatingSystemMXBean operatingSystemMXBean, final RuntimeMXBean runtimeMXBean) {
        return new Gauge<Double>() {

            private long lastCpuTimeNS = UNINITIALIZED;
            private long lastUpTimeMS = UNINITIALIZED;

            @Override
            public Double getValue() {

                final long cpuTimeNS = operatingSystemMXBean.getProcessCpuTime();
                if (cpuTimeNS == UNSUPPORTED) {
                    return UNSUPPORTED_GAUGE.getValue();
                }
                final long upTimeMS = runtimeMXBean.getUptime();

                if (this.lastCpuTimeNS == UNINITIALIZED || this.lastUpTimeMS == UNINITIALIZED) {
                    this.lastCpuTimeNS = cpuTimeNS;
                    this.lastUpTimeMS = upTimeMS;
                    return 0.0D;
                }

                final long totalCpuTimeNS = cpuTimeNS - lastCpuTimeNS;
                final long diffUpTimeMS = upTimeMS - lastUpTimeMS;
                final long totalUpTimeNS = (diffUpTimeMS * 1000000) * CPU_COUNT;

                final double cpuLoad = totalUpTimeNS > 0 ?
                        Math.min(100F, totalCpuTimeNS / (float)totalUpTimeNS) : UNSUPPORTED;

                this.lastCpuTimeNS = cpuTimeNS;
                this.lastUpTimeMS = upTimeMS;

                return cpuLoad;
            }
        };
    }
}
