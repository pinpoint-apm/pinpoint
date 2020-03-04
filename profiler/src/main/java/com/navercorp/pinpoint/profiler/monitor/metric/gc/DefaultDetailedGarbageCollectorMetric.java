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

package com.navercorp.pinpoint.profiler.monitor.metric.gc;

import java.lang.management.GarbageCollectorMXBean;

/**
 * @author HyunGil Jeong
 */
public class DefaultDetailedGarbageCollectorMetric implements DetailedGarbageCollectorMetric {

    private final GarbageCollectorType garbageCollectorType;
    private final GarbageCollectorMXBean garbageCollectorMXBean;

    public DefaultDetailedGarbageCollectorMetric(GarbageCollectorType garbageCollectorType, GarbageCollectorMXBean garbageCollectorMXBean) {
        if (garbageCollectorType == null) {
            throw new NullPointerException("garbageCollectorType");
        }
        if (garbageCollectorMXBean == null) {
            throw new NullPointerException("garbageCollectorMXBean");
        }
        this.garbageCollectorType = garbageCollectorType;
        this.garbageCollectorMXBean = garbageCollectorMXBean;
    }

    @Override
    public DetailedGarbageCollectorMetricSnapshot getSnapshot() {
        long gcNewCount = garbageCollectorMXBean.getCollectionCount();
        long gcNewTime = garbageCollectorMXBean.getCollectionTime();
        return new DetailedGarbageCollectorMetricSnapshot(gcNewCount, gcNewTime);
    }

    @Override
    public String toString() {
        return garbageCollectorType + " detailed garbage collector metric";
    }
}
