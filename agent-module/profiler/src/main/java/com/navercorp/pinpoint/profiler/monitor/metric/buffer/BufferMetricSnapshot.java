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

package com.navercorp.pinpoint.profiler.monitor.metric.buffer;

/**
 * @author Roy Kim
 */
public class BufferMetricSnapshot {

    private final long directCount;
    private final long directMemoryUsed;
    private final long mappedCount;
    private final long mappedMemoryUsed;

    public BufferMetricSnapshot(long directCount, long directMemoryUsed, long mappedCount, long mappedMemoryUsed){
        this.directCount = directCount;
        this.directMemoryUsed = directMemoryUsed;
        this.mappedCount = mappedCount;
        this.mappedMemoryUsed = mappedMemoryUsed;
    }

    public long getDirectCount() {
        return directCount;
    }

    public long getDirectMemoryUsed() {
        return directMemoryUsed;
    }

    public long getMappedCount() {
        return mappedCount;
    }

    public long getMappedMemoryUsed() {
        return mappedMemoryUsed;
    }

    @Override
    public String toString() {
        return "BufferMetricSnapshot{" +
                "directCount=" + directCount +
                ", directMemoryUsed=" + directMemoryUsed +
                ", mappedCount=" + mappedCount +
                ", mappedMemoryUsed=" + mappedMemoryUsed +
                '}';
    }
}
