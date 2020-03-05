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

package com.navercorp.pinpoint.profiler.monitor.metric.memory;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

/**
 * Metric for detailed memory usages
 *
 * @author dawidmalina
 * @author HyunGil Jeong
 */
public class DefaultDetailedMemoryMetric implements DetailedMemoryMetric {

    private final MemoryPoolType memoryPoolType;
    private final MemoryPoolMXBeanWrapper codeCachePool;
    private final MemoryPoolMXBeanWrapper edenSpacePool;
    private final MemoryPoolMXBeanWrapper oldSpacePool;
    private final MemoryPoolMXBeanWrapper survivorSpacePool;
    private final MemoryPoolMXBeanWrapper permGenPool;
    private final MemoryPoolMXBeanWrapper metaspacePool;

    public DefaultDetailedMemoryMetric(
            MemoryPoolType memoryPoolType,
            MemoryPoolMXBean edenSpacePool,
            MemoryPoolMXBean oldSpacePool,
            MemoryPoolMXBean survivorSpacePool,
            MemoryPoolMXBean codeCachePool,
            MemoryPoolMXBean permGenPool,
            MemoryPoolMXBean metaspacePool) {
        if (memoryPoolType == null) {
            throw new NullPointerException("memoryPoolType");
        }
        this.memoryPoolType = memoryPoolType;
        this.codeCachePool = wrap(codeCachePool);
        this.edenSpacePool = wrap(edenSpacePool);
        this.oldSpacePool = wrap(oldSpacePool);
        this.survivorSpacePool = wrap(survivorSpacePool);
        this.permGenPool = wrap(permGenPool);
        this.metaspacePool = wrap(metaspacePool);
    }

    @Override
    public DetailedMemoryMetricSnapshot getSnapshot() {
        double edenSpaceMemoryUsage = calculateUsage(edenSpacePool.getUsage());
        double oldSpaceMemoryUsage = calculateUsage(oldSpacePool.getUsage());
        double survivorSpaceMemoryUsage = calculateUsage(survivorSpacePool.getUsage());
        double codeCacheMemoryUsage = calculateUsage(codeCachePool.getUsage());
        double permGenMemoryUsage = calculateUsage(permGenPool.getUsage());
        double metaspaceMemoryUsage = calculateUsage(metaspacePool.getUsage());
        return new DetailedMemoryMetricSnapshot(
                edenSpaceMemoryUsage,
                oldSpaceMemoryUsage,
                survivorSpaceMemoryUsage,
                codeCacheMemoryUsage,
                permGenMemoryUsage,
                metaspaceMemoryUsage);
    }

    @Override
    public String toString() {
        return memoryPoolType + " detailed memory metric";
    }

    private double calculateUsage(MemoryUsage memoryUsage) {
        if (memoryUsage == null) {
            return UNCOLLECTED_USAGE;
        }
        long max = memoryUsage.getMax() == -1 ? memoryUsage.getCommitted() : memoryUsage.getMax();
        if (max == -1 || max == 0) {
            return UNCOLLECTED_USAGE;
        }
        return memoryUsage.getUsed() / (double) max;
    }

    private interface MemoryPoolMXBeanWrapper {
        MemoryUsage getUsage();
    }

    private static MemoryPoolMXBeanWrapper wrap(final MemoryPoolMXBean memoryPoolMXBean) {
        if (memoryPoolMXBean == null) {
            return new MemoryPoolMXBeanWrapper() {
                @Override
                public MemoryUsage getUsage() {
                    return null;
                }
            };
        } else {
            return new MemoryPoolMXBeanWrapper() {
                @Override
                public MemoryUsage getUsage() {
                    return memoryPoolMXBean.getUsage();
                }
            };
        }
    }
}
