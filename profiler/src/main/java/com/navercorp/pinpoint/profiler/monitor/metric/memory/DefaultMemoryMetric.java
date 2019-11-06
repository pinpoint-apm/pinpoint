/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.metric.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * @author HyunGil Jeong
 */
public class DefaultMemoryMetric implements MemoryMetric {

    private MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();


    public DefaultMemoryMetric() {
    }

    @Override
    public MemoryMetricSnapshot getSnapshot() {
        final MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        final MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        final long heapMax = heapMemoryUsage.getMax();
        final long heapUsed = heapMemoryUsage.getUsed();
        final long nonHeapMax = nonHeapMemoryUsage.getMax();
        final long nonHeapUsed = nonHeapMemoryUsage.getUsed();

        return new MemoryMetricSnapshot(heapMax, heapUsed, nonHeapMax, nonHeapUsed);
    }

    @Override
    public String toString() {
        return "Default memory metric";
    }

}
