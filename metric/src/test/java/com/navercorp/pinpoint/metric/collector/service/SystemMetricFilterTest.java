/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.collector.dao.pinot.PinotSystemMetricDoubleDao;
import com.navercorp.pinpoint.metric.collector.dao.pinot.PinotSystemMetricLongDao;
import com.navercorp.pinpoint.metric.common.model.DoubleCounter;
import com.navercorp.pinpoint.metric.common.model.LongCounter;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricFilterTest {
    private final Random random = new Random(System.currentTimeMillis());

    SystemMetricService systemMetricService;

    @Mock
    LongCounter longCounter;
    @Mock
    DoubleCounter doubleCounter;
    @Mock
    PinotSystemMetricLongDao longDao;
    @Mock
    PinotSystemMetricDoubleDao doubleDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        systemMetricService = new SystemMetricService(longDao, doubleDao);
    }

    @Test
    public void testFilter() {
        int longCount = random.nextInt(100);
        int doubleCount = random.nextInt(100);
        List<SystemMetric> systemMetricList = createList(longCount, doubleCount);

        List<LongCounter> longCounterList = systemMetricService.filterLongCounter(systemMetricList);
        List<DoubleCounter> doubleCounterList = systemMetricService.filterDoubleCounter(systemMetricList);

        Assert.assertEquals(longCount, longCounterList.size());
        Assert.assertEquals(doubleCount, doubleCounterList.size());
    }

    private List<SystemMetric> createList(int longCount, int doubleCount) {
        List<SystemMetric> systemMetricList = new ArrayList<>();

        for (int i = 0; i < longCount; i++) {
            systemMetricList.add(longCounter);
        }
        for (int i = 0; i < doubleCount; i++) {
            systemMetricList.add(doubleCounter);
        }

        return systemMetricList;
    }
}
