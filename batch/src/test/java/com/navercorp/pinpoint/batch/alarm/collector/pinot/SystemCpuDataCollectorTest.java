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
import com.navercorp.pinpoint.batch.alarm.vo.AgentUsageCount;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author minwoo-jung
 */
@ExtendWith({MockitoExtension.class})
class SystemCpuDataCollectorTest {

    @Mock
    AlarmDao alarmDao;

    @Test
    public void collect() {
        Application application = new Application("test", ServiceType.STAND_ALONE);
        long now = System.currentTimeMillis();

        List<AgentUsageCount> agentUsageCountList = List.of(
                new AgentUsageCount("testAgent1", 1000D, 20D),
                new AgentUsageCount("testAgent2", 2000D, 20D),
                new AgentUsageCount("testAgent3", 1500D,20D),
                new AgentUsageCount("testAgent4", 2000D, 20D),
                new AgentUsageCount("testAgent5", 1000D, 20D),
                new AgentUsageCount("testAgent6", 2000D, 20D)
        );

        when(alarmDao.selectSumCount(any(), any(), any(), any())).thenReturn(agentUsageCountList);

        SystemCpuDataCollector systemCpuDataCollector = new SystemCpuDataCollector(DataCollectorCategory.SYSTEM_CPU_USAGE_RATE, alarmDao, application, now, DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        systemCpuDataCollector.collect();

        Map<String, Long> systemCpuUsageRate = systemCpuDataCollector.getSystemCpuUsageRate();
        assertEquals(6, systemCpuUsageRate.size());
    }
}