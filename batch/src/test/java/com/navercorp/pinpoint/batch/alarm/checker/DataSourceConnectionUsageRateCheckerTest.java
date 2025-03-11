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

package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.collector.pinot.DataSourceDataCollector;
import com.navercorp.pinpoint.batch.alarm.vo.DataSourceAlarmVO;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
@ExtendWith(MockitoExtension.class)
public class DataSourceConnectionUsageRateCheckerTest {

    private static final String APPLICATION_NAME = "local_service";

    private static final String AGENT_ID = "local_tomcat";
    private static final String SERVICE_TYPE = "tomcat";

    private static final long CURRENT_TIME_MILLIS = System.currentTimeMillis();
    private static final long INTERVAL_MILLIS = 300000;
    private static final long START_TIME_MILLIS = CURRENT_TIME_MILLIS - INTERVAL_MILLIS;

    private static final List<String> mockAgentIds = List.of(AGENT_ID);

    private static final long TIMESTAMP_INTERVAL = 5000L;

    @Mock
    private DataSourceDataCollector dataSourceDataCollector;

    @Test
    public void checkTest1() {
        Rule rule = new Rule(APPLICATION_NAME, SERVICE_TYPE, CheckerCategory.ERROR_COUNT.getName(), 50, "testGroup", false, false, false, "");
        Map<String, List<DataSourceAlarmVO>> dataSourceAlarmVOMap = Map.ofEntries(
            Map.entry("local_tomcat", List.of(
                new DataSourceAlarmVO(1, "database1", 11, 20),
                new DataSourceAlarmVO(2, "database2", 20, 30),
                new DataSourceAlarmVO(3, "database3", 13, 40)
            ))
        );

        when(dataSourceDataCollector.getDataSourceConnectionUsageRate()).thenReturn(dataSourceAlarmVOMap);
        DataSourceConnectionUsageRateChecker checker = new DataSourceConnectionUsageRateChecker(dataSourceDataCollector, rule);

        checker.check();
        Assertions.assertTrue(checker.isDetected());

        String emailMessage = checker.getEmailMessage("pinpointUrl", "applicationName", "serviceType", "currentTime");
        Assertions.assertTrue(StringUtils.hasLength(emailMessage));

        List<String> smsMessage = checker.getSmsMessage();
        assertThat(smsMessage).hasSize(2);
    }

    @Test
    public void checkTest2() {
        Rule rule = new Rule(APPLICATION_NAME, SERVICE_TYPE, CheckerCategory.ERROR_COUNT.getName(), 50, "testGroup", false, false, false, "");
        Map<String, List<DataSourceAlarmVO>> dataSourceAlarmVOMap = Map.ofEntries(
                Map.entry("local_tomcat", List.of(
                        new DataSourceAlarmVO(1, "database1", 40, 100),
                        new DataSourceAlarmVO(2, "database2", 10, 100),
                        new DataSourceAlarmVO(3, "database3", 20, 100)
                ))
        );

        when(dataSourceDataCollector.getDataSourceConnectionUsageRate()).thenReturn(dataSourceAlarmVOMap);
        DataSourceConnectionUsageRateChecker checker = new DataSourceConnectionUsageRateChecker(dataSourceDataCollector, rule);

        checker.check();
        Assertions.assertFalse(checker.isDetected());

        String emailMessage = checker.getEmailMessage("pinpointUrl", "applicationName", "serviceType", "currentTime");
        Assertions.assertTrue(StringUtils.isEmpty(emailMessage));

        List<String> smsMessage = checker.getSmsMessage();
        assertThat(smsMessage).isEmpty();
    }

}
