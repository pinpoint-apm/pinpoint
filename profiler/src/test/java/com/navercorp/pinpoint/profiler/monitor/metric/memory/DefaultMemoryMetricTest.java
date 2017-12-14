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

import org.junit.Assert;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author HyunGil Jeong
 */
public class DefaultMemoryMetricTest {

    @Test
    public void testJvmSupplied() {
        // Given
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryMetric memoryMetric = new DefaultMemoryMetric(memoryMXBean);
        // When
        MemoryMetricSnapshot snapshot = memoryMetric.getSnapshot();
        // Then
        Assert.assertTrue(snapshot.getHeapMax() >= -1);
        Assert.assertTrue(snapshot.getHeapUsed() > 0);
        Assert.assertTrue(snapshot.getNonHeapMax() >= -1);
        Assert.assertTrue(snapshot.getNonHeapUsed() > 0);
    }

    @Test
    public void testNullMemoryUsage() {
        // Given
        MemoryMXBean memoryMXBean = mock(MemoryMXBean.class);
        when(memoryMXBean.getHeapMemoryUsage()).thenReturn(null);
        when(memoryMXBean.getNonHeapMemoryUsage()).thenReturn(null);
        MemoryMetric memoryMetric = new DefaultMemoryMetric(memoryMXBean);
        // When
        MemoryMetricSnapshot snapshot = memoryMetric.getSnapshot();
        // Then
        Assert.assertEquals(MemoryMetric.UNCOLLECTED_VALUE, snapshot.getHeapMax());
        Assert.assertEquals(MemoryMetric.UNCOLLECTED_VALUE, snapshot.getHeapUsed());
        Assert.assertEquals(MemoryMetric.UNCOLLECTED_VALUE, snapshot.getNonHeapMax());
        Assert.assertEquals(MemoryMetric.UNCOLLECTED_VALUE, snapshot.getNonHeapUsed());
    }
}
