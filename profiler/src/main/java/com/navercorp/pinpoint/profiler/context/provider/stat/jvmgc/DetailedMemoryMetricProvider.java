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

package com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.DefaultDetailedMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.DetailedMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.MemoryPoolType;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.UnknownDetailedMemoryMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dawidmalina
 * @author HyunGil Jeong
 */
public class DetailedMemoryMetricProvider implements Provider<DetailedMemoryMetric> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DetailedMemoryMetricProvider() {
    }

    @Override
    public DetailedMemoryMetric get() {
        DetailedMemoryMetric detailedMemoryMetric = null;
        Map<String, MemoryPoolMXBean> memoryPoolMap = createMemoryPoolMap();
        for (MemoryPoolType memoryPoolType : MemoryPoolType.values()) {
            if (memoryPoolMap.containsKey(memoryPoolType.oldSpace())) {
                detailedMemoryMetric = createMetric(memoryPoolMap, memoryPoolType);
                break;
            }
        }
        if (detailedMemoryMetric == null) {
            detailedMemoryMetric = new UnknownDetailedMemoryMetric();
        }
        logger.info("loaded : {}", detailedMemoryMetric);
        return detailedMemoryMetric;
    }

    private Map<String, MemoryPoolMXBean> createMemoryPoolMap() {
        Map<String, MemoryPoolMXBean> memoryPoolMap = new HashMap<String, MemoryPoolMXBean>();
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPool : memoryPools) {
            memoryPoolMap.put(memoryPool.getName(), memoryPool);
        }
        return memoryPoolMap;
    }

    private DetailedMemoryMetric createMetric(Map<String, MemoryPoolMXBean> memoryPools, MemoryPoolType poolType) {
        MemoryPoolMXBean edenSpacePool = memoryPools.get(poolType.edenSpace());
        MemoryPoolMXBean oldSpacePool = memoryPools.get(poolType.oldSpace());
        MemoryPoolMXBean survivorSpacePool = memoryPools.get(poolType.survivorSpace());
        MemoryPoolMXBean codeCachePool = memoryPools.get(poolType.codeCache());
        MemoryPoolMXBean permGenPool = memoryPools.get(poolType.permGen());
        MemoryPoolMXBean metaspacePool = memoryPools.get(poolType.metaspace());
        return new DefaultDetailedMemoryMetric(poolType, edenSpacePool, oldSpacePool, survivorSpacePool, codeCachePool, permGenPool, metaspacePool);
    }
}
