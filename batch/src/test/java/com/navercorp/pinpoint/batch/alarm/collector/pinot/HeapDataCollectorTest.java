/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm.collector.pinot;

import com.navercorp.pinpoint.batch.alarm.DataCollectorFactory;
import com.navercorp.pinpoint.batch.alarm.dao.AlarmDao;
import com.navercorp.pinpoint.batch.alarm.vo.AgentFieldUsage;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author minwoo-jung
 */
@ExtendWith({MockitoExtension.class})
class HeapDataCollectorTest {

    @Mock
    AlarmDao alarmDao;

    @Test
    public void collect() {
        Application application = new Application("test", ServiceType.STAND_ALONE);
        long now = System.currentTimeMillis();

        List<AgentFieldUsage> agentFieldUsageList = List.of(
                new AgentFieldUsage("testAgent1", HeapDataCollector.FIELD_HEAP_USED, 1000D),
                new AgentFieldUsage("testAgent1", HeapDataCollector.FIELD_HEAP_MAX, 2000D),
                new AgentFieldUsage("testAgent2", HeapDataCollector.FIELD_HEAP_USED, 1000D),
                new AgentFieldUsage("testAgent2", HeapDataCollector.FIELD_HEAP_MAX, 2000D),
                new AgentFieldUsage("testAgent3", HeapDataCollector.FIELD_HEAP_USED, 1000D),
                new AgentFieldUsage("testAgent3", HeapDataCollector.FIELD_HEAP_MAX, 2000D),
                new AgentFieldUsage("testAgent4", HeapDataCollector.FIELD_HEAP_USED, 1000D),
                new AgentFieldUsage("testAgent4", HeapDataCollector.FIELD_HEAP_MAX, 2000D)
        );

        when(alarmDao.selectSumGroupByField(any(), any(), any(), any())).thenReturn(agentFieldUsageList);

        HeapDataCollector heapDataCollector = new HeapDataCollector(DataCollectorCategory.HEAP_USAGE_RATE, alarmDao, application, now, DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        heapDataCollector.collect();

        Map<String, Long> heapUsageRate = heapDataCollector.getHeapUsageRate();
        assertEquals(4, heapUsageRate.size());
    }

}