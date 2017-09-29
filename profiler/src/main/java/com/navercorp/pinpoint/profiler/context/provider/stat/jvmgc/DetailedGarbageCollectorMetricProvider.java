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
import com.navercorp.pinpoint.profiler.monitor.metric.gc.DefaultDetailedGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.DetailedGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.GarbageCollectorType;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.UnknownDetailedGarbageCollectorMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class DetailedGarbageCollectorMetricProvider implements Provider<DetailedGarbageCollectorMetric> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public DetailedGarbageCollectorMetricProvider() {

    }

    @Override
    public DetailedGarbageCollectorMetric get() {
        DetailedGarbageCollectorMetric detailedGarbageCollectorMetric = null;
        Map<String, GarbageCollectorMXBean> garbageCollectorMap = createGarbageCollectorMap();
        for (GarbageCollectorType garbageCollectorType : GarbageCollectorType.values()) {
            if (garbageCollectorMap.containsKey(garbageCollectorType.oldGenName())) {
                GarbageCollectorMXBean garbageCollectorMXBean = garbageCollectorMap.get(garbageCollectorType.oldGenName());
                detailedGarbageCollectorMetric = new DefaultDetailedGarbageCollectorMetric(garbageCollectorType, garbageCollectorMXBean);
                break;
            }
        }
        if (detailedGarbageCollectorMetric == null) {
            detailedGarbageCollectorMetric = new UnknownDetailedGarbageCollectorMetric();
        }
        logger.info("loaded : {}", detailedGarbageCollectorMetric);
        return detailedGarbageCollectorMetric;
    }

    private Map<String, GarbageCollectorMXBean> createGarbageCollectorMap() {
        Map<String, GarbageCollectorMXBean> garbageCollectorMap = new HashMap<String, GarbageCollectorMXBean>();
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            garbageCollectorMap.put(garbageCollectorMXBean.getName(), garbageCollectorMXBean);
        }
        return garbageCollectorMap;
    }
}
