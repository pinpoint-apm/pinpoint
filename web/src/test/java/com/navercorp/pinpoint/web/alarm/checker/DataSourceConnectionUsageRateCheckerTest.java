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

package com.navercorp.pinpoint.web.alarm.checker;

import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.DataSourceListBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorFactory;
import com.navercorp.pinpoint.web.alarm.collector.DataSourceDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSourceConnectionUsageRateCheckerTest {

    private static final String APPLICATION_NAME = "local_service";

    private static final String AGENT_ID = "local_tomcat";
    private static final String SERVICE_TYPE = "tomcat";

    private static final long CURRENT_TIME_MILLIS = System.currentTimeMillis();
    private static final long INTERVAL_MILLIS = 300000;
    private static final long START_TIME_MILLIS = CURRENT_TIME_MILLIS - INTERVAL_MILLIS;


    private static final long TIMESTAMP_INTERVAL = 5000L;

    @Mock
    private AgentStatDao<DataSourceListBo> mockDataSourceDao;

    @Mock
    private ApplicationIndexDao mockApplicationIndexDao;

    @Before
    public void before() {
        when(mockApplicationIndexDao.selectAgentIds(APPLICATION_NAME)).thenReturn(Arrays.asList(AGENT_ID));

        Range range = Range.createUncheckedRange(START_TIME_MILLIS, CURRENT_TIME_MILLIS);

        List<DataSourceListBo> dataSourceListBoList = new ArrayList<>();
        dataSourceListBoList.add(createDataSourceListBo(1, 30, 40, 3));
        dataSourceListBoList.add(createDataSourceListBo(2, 25, 40, 3));
        dataSourceListBoList.add(createDataSourceListBo(3, 10, 40, 3));

        when(mockDataSourceDao.getAgentStatList(AGENT_ID, range)).thenReturn(dataSourceListBoList);
    }

    @Test
    public void checkTest1() {
        Rule rule = new Rule(APPLICATION_NAME, SERVICE_TYPE, CheckerCategory.ERROR_COUNT.getName(), 50, "testGroup", false, false, "");
        Application application = new Application(APPLICATION_NAME, ServiceType.STAND_ALONE);

        DataSourceDataCollector collector = new DataSourceDataCollector(DataCollectorFactory.DataCollectorCategory.DATA_SOURCE_STAT, application, mockDataSourceDao, mockApplicationIndexDao, CURRENT_TIME_MILLIS, INTERVAL_MILLIS);
        DataSourceConnectionUsageRateChecker checker = new DataSourceConnectionUsageRateChecker(collector, rule);
        checker.check();
        Assert.assertTrue(checker.isDetected());

        String emailMessage = checker.getEmailMessage();
        Assert.assertTrue(StringUtils.hasLength(emailMessage));

        List<String> smsMessage = checker.getSmsMessage();
        Assert.assertTrue(smsMessage.size() == 2);
    }

    @Test
    public void checkTest2() {
        Rule rule = new Rule(APPLICATION_NAME, SERVICE_TYPE, CheckerCategory.ERROR_COUNT.getName(), 80, "testGroup", false, false, "");
        Application application = new Application(APPLICATION_NAME, ServiceType.STAND_ALONE);

        DataSourceDataCollector collector = new DataSourceDataCollector(DataCollectorFactory.DataCollectorCategory.DATA_SOURCE_STAT, application, mockDataSourceDao, mockApplicationIndexDao, CURRENT_TIME_MILLIS, INTERVAL_MILLIS);
        DataSourceConnectionUsageRateChecker checker = new DataSourceConnectionUsageRateChecker(collector, rule);
        checker.check();
        Assert.assertFalse(checker.isDetected());

        String emailMessage = checker.getEmailMessage();
        Assert.assertTrue(StringUtils.isEmpty(emailMessage));

        List<String> smsMessage = checker.getSmsMessage();
        Assert.assertTrue(CollectionUtils.isEmpty(smsMessage));
    }

    private DataSourceListBo createDataSourceListBo(int id, int activeConnectionSize, int maxConnectionSize, int numValues) {
        DataSourceListBo dataSourceListBo = new DataSourceListBo();
        dataSourceListBo.setAgentId(AGENT_ID);
        dataSourceListBo.setStartTimestamp(START_TIME_MILLIS);
        dataSourceListBo.setTimestamp(CURRENT_TIME_MILLIS);

        List<Long> timestamps = createTimestamps(CURRENT_TIME_MILLIS, numValues);

        for (int i = 0; i < numValues; i++) {
            DataSourceBo dataSourceBo = new DataSourceBo();
            dataSourceBo.setAgentId(AGENT_ID);
            dataSourceBo.setStartTimestamp(START_TIME_MILLIS);
            dataSourceBo.setTimestamp(timestamps.get(i));

            dataSourceBo.setId(id);
            dataSourceBo.setServiceTypeCode(ServiceType.UNKNOWN.getCode());
            dataSourceBo.setDatabaseName("name-" + id);
            dataSourceBo.setJdbcUrl("jdbcurl-" + id);
            dataSourceBo.setActiveConnectionSize(activeConnectionSize);
            dataSourceBo.setMaxConnectionSize(maxConnectionSize);

            dataSourceListBo.add(dataSourceBo);
        }

        return dataSourceListBo;
    }

    private List<Long> createTimestamps(long initialTimestamp, int numValues) {
        long minTimestampInterval = TIMESTAMP_INTERVAL - 5L;
        long maxTimestampInterval = TIMESTAMP_INTERVAL + 5L;
        return createIncreasingValues(initialTimestamp, initialTimestamp, minTimestampInterval, maxTimestampInterval, numValues);
    }

    private List<Long> createIncreasingValues(Long minValue, Long maxValue, Long minIncrement, Long maxIncrement, int numValues) {
        List<Long> values = new ArrayList<Long>(numValues);
        long value = RandomUtils.nextLong(minValue, maxValue);
        values.add(value);
        for (int i = 0; i < numValues - 1; ++i) {
            long increment = RandomUtils.nextLong(minIncrement, maxIncrement);
            value = value + increment;
            values.add(value);
        }
        return values;
    }

}
