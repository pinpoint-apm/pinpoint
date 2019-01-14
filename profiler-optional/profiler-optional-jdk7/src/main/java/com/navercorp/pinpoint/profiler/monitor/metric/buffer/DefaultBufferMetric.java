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

import javax.management.ObjectName;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * @author Roy Kim
 */
public class DefaultBufferMetric implements BufferMetric {

    private final BufferPoolMXBean direct;
    private final BufferPoolMXBean mapped;

    public DefaultBufferMetric() {
        List<BufferPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        this.direct = getBufferPool(pools, BufferType.DIRECT.getName());
        this.mapped = getBufferPool(pools, BufferType.MAPPED.getName());
    }

    private BufferPoolMXBean getBufferPool(List<BufferPoolMXBean> pools, String type) {
        for (BufferPoolMXBean pool : pools) {
            final String poolName = pool.getName();
            if (poolName.equals(type)) {
                return pool;
            }
        }
        return new EmptyBufferPoolMXBean();
    }

    @Override
    public BufferMetricSnapshot getSnapshot() {

        final long directCount = direct.getCount();
        final long directMemoryUsed = direct.getMemoryUsed();
        final long mappedCount = mapped.getCount();
        final long mappedMemoryUsed = mapped.getMemoryUsed();

        return new BufferMetricSnapshot(directCount, directMemoryUsed, mappedCount, mappedMemoryUsed);
    }

    @Override
    public String toString() {
        return "DefaultBufferMetric";
    }

    private static class EmptyBufferPoolMXBean implements BufferPoolMXBean {
        @Override
        public ObjectName getObjectName() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public long getCount() {
            return UNCOLLECTED_VALUE;
        }

        @Override
        public long getTotalCapacity() {
            return UNCOLLECTED_VALUE;
        }

        @Override
        public long getMemoryUsed() {
            return UNCOLLECTED_VALUE;
        }
    }
}
