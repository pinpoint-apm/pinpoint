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

import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.DetailedMemoryMetricProvider;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author HyunGil Jeong
 */
public class DefaultDetailedMemoryMetricTest {

    DetailedMemoryMetricProvider detailedMemoryMetricProvider = new DetailedMemoryMetricProvider();

    @Test
    public void testJvmSupplied() {
        // Given
        DetailedMemoryMetric detailedMemoryMetric = detailedMemoryMetricProvider.get();
        // When
        DetailedMemoryMetricSnapshot snapshot = detailedMemoryMetric.getSnapshot();
        // Then
        Assert.assertNotEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getNewGenUsage());
        Assert.assertNotEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getOldGenUsage());
        Assert.assertNotEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getSurvivorSpaceUsage());
        Assert.assertNotEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getCodeCacheUsage());
        if (DetailedMemoryMetric.UNCOLLECTED_USAGE != snapshot.getPermGenUsage()) {
            Assert.assertTrue(DetailedMemoryMetric.UNCOLLECTED_USAGE == snapshot.getMetaspaceUsage());
        }
        if (DetailedMemoryMetric.UNCOLLECTED_USAGE != snapshot.getMetaspaceUsage()) {
            Assert.assertTrue(DetailedMemoryMetric.UNCOLLECTED_USAGE == snapshot.getPermGenUsage());
        }
    }

    @Test
    public void testNullMemoryPoolMXBeans() {
        // Given
        DetailedMemoryMetric detailedMemoryMetric = new DefaultDetailedMemoryMetric(MemoryPoolType.CMS, null, null, null, null, null, null);
        // When
        DetailedMemoryMetricSnapshot snapshot = detailedMemoryMetric.getSnapshot();
        // Then
        Assert.assertTrue(DetailedMemoryMetric.UNCOLLECTED_USAGE == snapshot.getNewGenUsage());
        Assert.assertTrue(DetailedMemoryMetric.UNCOLLECTED_USAGE == snapshot.getOldGenUsage());
        Assert.assertTrue(DetailedMemoryMetric.UNCOLLECTED_USAGE == snapshot.getSurvivorSpaceUsage());
        Assert.assertTrue(DetailedMemoryMetric.UNCOLLECTED_USAGE == snapshot.getCodeCacheUsage());
        Assert.assertTrue(DetailedMemoryMetric.UNCOLLECTED_USAGE == snapshot.getPermGenUsage());
        Assert.assertTrue(DetailedMemoryMetric.UNCOLLECTED_USAGE == snapshot.getMetaspaceUsage());
    }
}
