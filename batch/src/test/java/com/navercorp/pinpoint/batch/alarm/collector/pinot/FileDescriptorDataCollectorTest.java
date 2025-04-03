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
import com.navercorp.pinpoint.batch.alarm.vo.AgentUsage;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.vo.Application;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * @author minwoo-jung
 */
@ExtendWith({MockitoExtension.class})
class FileDescriptorDataCollectorTest {

    @Mock
    AlarmDao alarmDao;

    @Test
    public void collect() {
        Application application = new Application("test", ServiceType.STAND_ALONE);
        long now = System.currentTimeMillis();
        Range range = Range.unchecked(now - DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN, now);
        FileDescriptorDataCollector fileDescriptorDataCollector = new FileDescriptorDataCollector(DataCollectorCategory.FILE_DESCRIPTOR, alarmDao, application, now, DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);

        List<AgentUsage> agentUsageList = List.of(
                new AgentUsage("testAgent1", 100D),
                new AgentUsage("testAgent2", 200D),
                new AgentUsage("testAgent3", 150D),
                new AgentUsage("testAgent4", 200D),
                new AgentUsage("testAgent5", 300D),
                new AgentUsage("testAgent6", 400D)
        );
        when(alarmDao.selectAvg(application.getName(), FileDescriptorDataCollector.METRIC_NAME, FileDescriptorDataCollector.FIELD_NAME, range)).thenReturn(agentUsageList);
        fileDescriptorDataCollector.collect();

        Map<String, Long> fileDescriptorCount = fileDescriptorDataCollector.getFileDescriptorCount();
        Assertions.assertThat(fileDescriptorCount)
                .hasSize(6)
                .containsEntry("testAgent1", 100L)
                .containsEntry("testAgent2", 200L);
    }
}