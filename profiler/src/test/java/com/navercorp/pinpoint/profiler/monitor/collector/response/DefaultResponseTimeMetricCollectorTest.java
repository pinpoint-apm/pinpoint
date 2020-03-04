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

package com.navercorp.pinpoint.profiler.monitor.collector.response;

import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;
import com.navercorp.pinpoint.thrift.dto.TResponseTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
            long value = Math.max(500, (random.nextLong() % 2500) + 500);
            totalValue += value;
        }

        when(responseTimeValue.getAvg()).thenReturn(totalValue / COUNT);

        when(responseTimeMetric.responseTimeValue()).thenReturn(responseTimeValue);
    }

    @Test
    public void defaultTest() throws Exception {
        AgentStatMetricCollector<ResponseTimeValue> responseTimeMetricCollector = new DefaultResponseTimeMetricCollector(responseTimeMetric);
        ResponseTimeValue collect = responseTimeMetricCollector.collect();

        Assert.assertEquals(totalValue / COUNT, collect.getAvg());
    }

    @Test(expected = NullPointerException.class)
    public void throwNPETest() throws Exception {
        AgentStatMetricCollector<ResponseTimeValue> responseTimeMetricCollector = new DefaultResponseTimeMetricCollector(null);
    }

}
