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

package com.navercorp.pinpoint.profiler.monitor.metric.gc;


import java.lang.management.GarbageCollectorMXBean;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class DefaultGarbageCollectorMetric implements GarbageCollectorMetric {

    private final GarbageCollectorType garbageCollectorType;
    private final GarbageCollectorMXBean garbageCollectorMXBean;

    public DefaultGarbageCollectorMetric(GarbageCollectorType garbageCollectorType, GarbageCollectorMXBean garbageCollectorMXBean) {
        this.garbageCollectorType = Objects.requireNonNull(garbageCollectorType, "garbageCollectorType");
        this.garbageCollectorMXBean = Objects.requireNonNull(garbageCollectorMXBean, "garbageCollectorMXBean");
    }

    @Override
    public JvmGcType getGcType() {
        return garbageCollectorType.jvmGcType();
    }

    @Override
    public GarbageCollectorMetricSnapshot getSnapshot() {
        long gcCount = garbageCollectorMXBean.getCollectionCount();
        long gcTime = garbageCollectorMXBean.getCollectionTime();
        return new GarbageCollectorMetricSnapshot(gcCount, gcTime);
    }

    @Override
    public String toString() {
        return garbageCollectorType + " garbage collector metric";
    }
}
