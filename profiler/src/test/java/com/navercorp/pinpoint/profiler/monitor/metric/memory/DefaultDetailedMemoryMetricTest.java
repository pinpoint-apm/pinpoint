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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        Assertions.assertNotEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getNewGenUsage());
        Assertions.assertNotEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getOldGenUsage());
        Assertions.assertNotEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getSurvivorSpaceUsage());
        Assertions.assertNotEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getCodeCacheUsage());
        if (DetailedMemoryMetric.UNCOLLECTED_USAGE != snapshot.getPermGenUsage()) {
            Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getMetaspaceUsage(), 0.0);
        }
        if (DetailedMemoryMetric.UNCOLLECTED_USAGE != snapshot.getMetaspaceUsage()) {
            Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getPermGenUsage(), 0.0);
        }
    }

    @Test
    public void testNullMemoryPoolMXBeans() {
        // Given
        DetailedMemoryMetric detailedMemoryMetric = new DefaultDetailedMemoryMetric(MemoryPoolType.CMS, null, null, null, null, null, null);
        // When
        DetailedMemoryMetricSnapshot snapshot = detailedMemoryMetric.getSnapshot();
        // Then
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getNewGenUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getOldGenUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getSurvivorSpaceUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getCodeCacheUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getPermGenUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getMetaspaceUsage(), 0.0);
    }

    @Test
    public void testNullMemoryUsage() {
        // Given
        MemoryPoolMXBean mockMXBean = mock(MemoryPoolMXBean.class);
        MemoryUsage nullMemoryUsage = null;
        when(mockMXBean.getUsage()).thenReturn(nullMemoryUsage);
        DetailedMemoryMetric detailedMemoryMetric = new DefaultDetailedMemoryMetric(MemoryPoolType.CMS, mockMXBean, mockMXBean, mockMXBean, mockMXBean, mockMXBean, mockMXBean);
        // When
        DetailedMemoryMetricSnapshot snapshot = detailedMemoryMetric.getSnapshot();
        // Then
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getNewGenUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getOldGenUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getSurvivorSpaceUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getCodeCacheUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getPermGenUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getMetaspaceUsage(), 0.0);
    }

    @Test
    public void testUnknownMax() {
        // Given
        MemoryPoolMXBean mockMXBean = mock(MemoryPoolMXBean.class);
        MemoryUsage mockUsage = mock(MemoryUsage.class);
        when(mockMXBean.getUsage()).thenReturn(mockUsage);
        when(mockUsage.getMax()).thenReturn(-1L);
        DetailedMemoryMetric detailedMemoryMetric = new DefaultDetailedMemoryMetric(MemoryPoolType.CMS, mockMXBean, mockMXBean, mockMXBean, mockMXBean, mockMXBean, mockMXBean);
        // When
        DetailedMemoryMetricSnapshot snapshot = detailedMemoryMetric.getSnapshot();
        // Then
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getNewGenUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getOldGenUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getSurvivorSpaceUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getCodeCacheUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getPermGenUsage(), 0.0);
        Assertions.assertEquals(DetailedMemoryMetric.UNCOLLECTED_USAGE, snapshot.getMetaspaceUsage(), 0.0);
    }

    @Test
    public void testValidMax() {
        // Given
        long givenUsed = 50L;
        long givenMax = 100L;
        double expectedUsage = givenUsed / (double) givenMax;
        MemoryPoolMXBean mockMXBean = mock(MemoryPoolMXBean.class);
        MemoryUsage mockUsage = mock(MemoryUsage.class);
        when(mockMXBean.getUsage()).thenReturn(mockUsage);
        when(mockUsage.getUsed()).thenReturn(50L);
        when(mockUsage.getMax()).thenReturn(100L);
        DetailedMemoryMetric detailedMemoryMetric = new DefaultDetailedMemoryMetric(MemoryPoolType.CMS, mockMXBean, mockMXBean, mockMXBean, mockMXBean, mockMXBean, mockMXBean);
        // When
        DetailedMemoryMetricSnapshot snapshot = detailedMemoryMetric.getSnapshot();
        // Then
        Assertions.assertEquals(expectedUsage, snapshot.getNewGenUsage(), 0.01);
        Assertions.assertEquals(expectedUsage, snapshot.getOldGenUsage(), 0.01);
        Assertions.assertEquals(expectedUsage, snapshot.getSurvivorSpaceUsage(), 0.01);
        Assertions.assertEquals(expectedUsage, snapshot.getCodeCacheUsage(), 0.01);
        Assertions.assertEquals(expectedUsage, snapshot.getPermGenUsage(), 0.01);
        Assertions.assertEquals(expectedUsage, snapshot.getMetaspaceUsage(), 0.01);
    }
}
