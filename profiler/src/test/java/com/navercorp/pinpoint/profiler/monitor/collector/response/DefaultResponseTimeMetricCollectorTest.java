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

package com.navercorp.pinpoint.profiler.monitor.collector.response;

import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeMetric;
import com.navercorp.pinpoint.thrift.dto.TResponseTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Random;

import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultResponseTimeMetricCollectorTest {

    private final Random random = new Random(System.currentTimeMillis());

    private final long COUNT = 3;
    private long totalValue;

    @Mock
    private ResponseTimeValue responseTimeValue;

    @Mock
    private ResponseTimeMetric responseTimeMetric;

    @Before
    public void setUp() {
        totalValue = 0;
        for (int i = 0; i < COUNT; i++) {
            long value = Math.max(500, random.nextLong() % 3000);
            totalValue += value;
        }

        when(responseTimeValue.getTotalResponseTime()).thenReturn(totalValue);

        when(responseTimeMetric.totalResponseTimeValue()).thenReturn(responseTimeValue);
    }

    @Test
    public void defaultTest() throws Exception {
        ResponseTimeMetricCollector responseTimeMetricCollector = new DefaultResponseTimeMetricCollector(responseTimeMetric);
        TResponseTime collect = responseTimeMetricCollector.collect();

        Assert.assertEquals(totalValue, collect.getTotalResponseTime());
    }

    @Test(expected = NullPointerException.class)
    public void throwNPETest() throws Exception {
        ResponseTimeMetricCollector responseTimeMetricCollector = new DefaultResponseTimeMetricCollector(null);
    }

}
