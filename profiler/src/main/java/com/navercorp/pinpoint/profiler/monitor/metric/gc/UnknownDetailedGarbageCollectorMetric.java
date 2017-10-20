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

/**
 * @author HyunGil Jeong
 */
public class UnknownDetailedGarbageCollectorMetric implements DetailedGarbageCollectorMetric {

    private static final DetailedGarbageCollectorMetricSnapshot UNSUPPORTED_SNAPSHOT = new DetailedGarbageCollectorMetricSnapshot(UNCOLLECTED_VALUE, UNCOLLECTED_VALUE);

    @Override
    public DetailedGarbageCollectorMetricSnapshot getSnapshot() {
        return UNSUPPORTED_SNAPSHOT;
    }

    @Override
    public String toString() {
        return "Unknown detailed garbage collector metric";
    }

}
