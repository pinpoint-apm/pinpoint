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

package com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric.oracle;

import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.codahale.metrics.Gauge;
import com.navercorp.pinpoint.profiler.monitor.codahale.cpu.metric.CpuLoadMetricSet;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

/**
 * @author HyunGil Jeong
 */
public class DefaultCpuLoadMetricSet extends CpuLoadMetricSet {
    private static final Double UNSUPPORTED_CPU_LOAD_METRIC = -1.0D;

    private final OperatingSystemMXBean operatingSystemMXBean;

    public DefaultCpuLoadMetricSet() {
        this.operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    protected Gauge<Double> getJvmCpuLoadGauge() {
        return new Gauge<Double>() {
            @Override
            public Double getValue() {
                Double value = UNSUPPORTED_CPU_LOAD_METRIC;
                try {
                    Method method = operatingSystemMXBean.getClass().getMethod("getProcessCpuLoad");
                    Object result = method.invoke(operatingSystemMXBean);
                    value = (Double) result;
                } catch (SecurityException e) {
                } catch (NoSuchMethodException e) {
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                } catch (ClassCastException e) {
                }
                return value;
            }
        };
    }

    @Override
    protected Gauge<Double> getSystemCpuLoadGauge() {
        return new Gauge<Double>() {
            @Override
            public Double getValue() {
                Double value = UNSUPPORTED_CPU_LOAD_METRIC;
                try {
                    Method method = operatingSystemMXBean.getClass().getMethod("getSystemCpuLoad");
                    Object result = method.invoke(operatingSystemMXBean);
                    value = (Double) result;
                } catch (SecurityException e) {
                } catch (NoSuchMethodException e) {
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                } catch (ClassCastException e) {
                }
                return value;
            }
        };
    }

    @Override
    public String toString() {
        return "CpuLoadMetricSet for Oracle Java 1.7+";
    }
}
