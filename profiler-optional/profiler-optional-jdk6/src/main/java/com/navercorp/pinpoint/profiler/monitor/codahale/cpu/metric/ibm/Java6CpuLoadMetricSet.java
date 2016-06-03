/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric.ibm;

import com.codahale.metrics.Gauge;
import com.ibm.lang.management.OperatingSystemMXBean;
import com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric.CpuLoadMetricSet;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author HyunGil Jeong
 */
public class Java6CpuLoadMetricSet extends CpuLoadMetricSet {

    private static final int UNSUPPORTED = -1;
    private static final int UNINITIALIZED = -1;
    private static final Double UNSUPPORTED_CPU_LOAD_METRIC = -1.0D;

    private final OperatingSystemMXBean operatingSystemMXBean;
    private final RuntimeMXBean runtimeMXBean;

    public Java6CpuLoadMetricSet() {
        this.operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    }

    @Override
    protected Gauge<Double> getJvmCpuLoadGauge() {
        return new Gauge<Double>() {

            private long lastCpuTimeNS = UNINITIALIZED;
            private long lastUpTimeMS = UNINITIALIZED;

            @Override
            public Double getValue() {

                final long cpuTimeNS = operatingSystemMXBean.getProcessCpuTimeByNS();
                if (cpuTimeNS == UNSUPPORTED) {
                    return UNSUPPORTED_CPU_LOAD_METRIC;
                }
                final long upTimeMS = runtimeMXBean.getUptime();

                if (this.lastCpuTimeNS == UNINITIALIZED || this.lastUpTimeMS == UNINITIALIZED) {
                    this.lastCpuTimeNS = cpuTimeNS;
                    this.lastUpTimeMS = upTimeMS;
                    return 0.0D;
                }

                final long totalCpuTimeNS = cpuTimeNS - lastCpuTimeNS;
                final long diffUpTimeMS = upTimeMS - lastUpTimeMS;
                final int numProcessors = Runtime.getRuntime().availableProcessors();
                final long totalUpTimeNS = (diffUpTimeMS * 1000000) * numProcessors;

                final double cpuLoad = totalUpTimeNS > 0 ?
                        Math.min(100F, totalCpuTimeNS / (float)totalUpTimeNS) : UNSUPPORTED;

                this.lastCpuTimeNS = cpuTimeNS;
                this.lastUpTimeMS = upTimeMS;

                return cpuLoad;
            }
        };
    }

    @Override
    protected Gauge<Double> getSystemCpuLoadGauge() {
        return new Gauge<Double>() {
            @Override
            public Double getValue() {
                return operatingSystemMXBean.getSystemCpuLoad();
            }
        };
    }

    @Override
    public String toString() {
        return "CpuLoadMetricSet for IBM Java 1.6";
    }
}
