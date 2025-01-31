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
import com.navercorp.pinpoint.batch.alarm.vo.DataSourceAlarmVO;
import com.navercorp.pinpoint.common.model.TagInformation;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.metric.common.model.Tag;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;



/**
 * @author minwoo-jung
 */
@ExtendWith({MockitoExtension.class})
class DataSourceDataCollectorTest {


    @Mock
    private AlarmDao alarmDao;

    @Test
    public void collect() {
        final String applicationName = "testApplication";
        Application application = new Application(applicationName, ServiceType.STAND_ALONE);
        long now = System.currentTimeMillis();
        Range range = Range.unchecked(now - DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN, now);

        final String testAgent1 = "testAgent1";
        final String testAgent2 = "testAgent2";
        List<String> agentIds = List.of(testAgent1, testAgent2);

        when(alarmDao.selectTagInfo(any(), any(), any(), any(), any())).thenReturn(CompletableFuture.completedFuture(List.of(new Tag("jdbcUrl", "jdbc:mysql://localhost:3306/testdb"))));

        when(alarmDao.getTagInfoContainedSpecificTag(any(), eq(testAgent1), any(), any(), any(), any())).
        thenReturn(CompletableFuture.completedFuture(
                List.of(
                        new TagInformation(applicationName, testAgent1, DataSourceDataCollector.METRIC_NAME, DataSourceDataCollector.FIELD_ACTIVE_CONNECTION, List.of(new Tag("jdbcUrl", "jdbc:mysql://localhost:3306/testdb")))
                        )));

        when(alarmDao.getTagInfoContainedSpecificTag(any(), eq(testAgent2), any(), any(), any(), any())).
        thenReturn(CompletableFuture.completedFuture(
                List.of(
                        new TagInformation(applicationName, testAgent2, DataSourceDataCollector.METRIC_NAME, DataSourceDataCollector.FIELD_ACTIVE_CONNECTION, List.of(new Tag("jdbcUrl", "jdbc:mysql://localhost:3306/testdb")))
                )));

        when(alarmDao.selectAvgGroupByField(any(), eq(testAgent1), any(), any(), any(), any())).
        thenReturn(CompletableFuture.completedFuture(List.of(new AgentFieldUsage(testAgent1, DataSourceDataCollector.FIELD_ACTIVE_CONNECTION, 20D),
                                                             new AgentFieldUsage(testAgent1, DataSourceDataCollector.FIELD_MAX_CONNECTION, 40D))));;

        when(alarmDao.selectAvgGroupByField(any(), eq(testAgent2), any(), any(), any(), any())).
                thenReturn(CompletableFuture.completedFuture(List.of(new AgentFieldUsage(testAgent2, DataSourceDataCollector.FIELD_ACTIVE_CONNECTION, 20D),
                                                                     new AgentFieldUsage(testAgent2, DataSourceDataCollector.FIELD_MAX_CONNECTION, 40D))));;

        DataSourceDataCollector dataSourceDataCollector = new DataSourceDataCollector(DataCollectorCategory.DATA_SOURCE_STAT, alarmDao, application, agentIds, now, DataCollectorFactory.SLOT_INTERVAL_FIVE_MIN);
        dataSourceDataCollector.collect();

        Map<String, List<DataSourceAlarmVO>> dataSourceConnectionUsageRate = dataSourceDataCollector.getDataSourceConnectionUsageRate();
        assertEquals(dataSourceConnectionUsageRate.size(), 2);
    }

}