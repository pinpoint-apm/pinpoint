/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.directbuffer;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * @author Roy Kim
 */
public class DefaultDirectBufferMetric implements DirectBufferMetric {

    public DefaultDirectBufferMetric() {
    }

    @Override
    public DirectBufferMetricSnapshot getSnapshot() {

        long directCount = UNCOLLECTED_VALUE;
        long directMemoryUsed = UNCOLLECTED_VALUE;
        long mappedCount = UNCOLLECTED_VALUE;
        long mappedMemoryUsed = UNCOLLECTED_VALUE;

        List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        for (BufferPoolMXBean pool : pools) {
            if (pool.getName() == DirectBufferType.DIRECT.genName()) {
                directCount = pool.getCount();
                directMemoryUsed = pool.getMemoryUsed();
            } else if (pool.getName() == DirectBufferType.MAPPED.genName()) {
                mappedCount = pool.getCount();
                mappedMemoryUsed = pool.getMemoryUsed();
            }
        }
        return new DirectBufferMetricSnapshot(directCount, directMemoryUsed, mappedCount, mappedMemoryUsed);
    }

    @Override
    public String toString() {
        return  "default direct buffer metric";
    }
}
