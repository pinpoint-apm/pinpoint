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

/**
 * @author HyunGil Jeong
 */
public class MemoryMetricSnapshot {

    private final long heapMax;
    private final long heapUsed;
    private final long nonHeapMax;
    private final long nonHeapUsed;

    MemoryMetricSnapshot(long heapMax, long heapUsed, long nonHeapMax, long nonHeapUsed) {
        this.heapMax = heapMax;
        this.heapUsed = heapUsed;
        this.nonHeapMax = nonHeapMax;
        this.nonHeapUsed = nonHeapUsed;
    }

    public long getHeapMax() {
        return heapMax;
    }

    public long getHeapUsed() {
        return heapUsed;
    }

    public long getNonHeapMax() {
        return nonHeapMax;
    }

    public long getNonHeapUsed() {
        return nonHeapUsed;
    }
}
