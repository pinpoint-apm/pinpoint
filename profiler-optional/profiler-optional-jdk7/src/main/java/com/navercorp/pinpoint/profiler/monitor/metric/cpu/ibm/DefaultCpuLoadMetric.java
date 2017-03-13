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

import com.codahale.metrics.Gauge;
import com.ibm.lang.management.OperatingSystemMXBean;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;

import java.lang.management.ManagementFactory;

/**
 * @author HyunGil Jeong
 */
public class DefaultCpuLoadMetric implements CpuLoadMetric {

    private final Gauge<Double> jvmCpuLoadGauge;
    private final Gauge<Double> systemCpuLoadGauge;

    public DefaultCpuLoadMetric() {
        final OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        jvmCpuLoadGauge = new Gauge<Double>() {
            @Override
            public Double getValue() {
                return operatingSystemMXBean.getProcessCpuLoad();
            }
        };
        systemCpuLoadGauge = new Gauge<Double>() {
            @Override
            public Double getValue() {
                return operatingSystemMXBean.getSystemCpuLoad();
            }
        };
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
        return "CpuLoadMetric for IBM Java 1.7+";
    }
}
