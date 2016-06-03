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

/**
 * @author HyunGil Jeong
 */
public class DefaultCpuLoadMetricSet extends CpuLoadMetricSet {

    private final OperatingSystemMXBean operatingSystemMXBean;

    public DefaultCpuLoadMetricSet() {
        this.operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    protected Gauge<Double> getJvmCpuLoadGauge() {
        return new Gauge<Double>() {
            @Override
            public Double getValue() {
                return operatingSystemMXBean.getProcessCpuLoad();
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
        return "CpuLoadMetricSet for IBM Java 1.7+";
    }
}
