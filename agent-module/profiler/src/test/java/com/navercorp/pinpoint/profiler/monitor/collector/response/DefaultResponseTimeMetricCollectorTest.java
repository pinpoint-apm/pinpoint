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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Random;

import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
@ExtendWith(MockitoExtension.class)
public class DefaultResponseTimeMetricCollectorTest {

    private final Random random = new Random(System.currentTimeMillis());

    private final long COUNT = 3;
    private long totalValue;

    @Mock
    private ResponseTimeValue responseTimeValue;

    @Mock
    private ResponseTimeMetric responseTimeMetric;

    @BeforeEach
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

        Assertions.assertEquals(totalValue / COUNT, collect.getAvg());
    }

    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    public void throwNPETest() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> {
            AgentStatMetricCollector<ResponseTimeValue> responseTimeMetricCollector = new DefaultResponseTimeMetricCollector(null);
        });
    }

}
