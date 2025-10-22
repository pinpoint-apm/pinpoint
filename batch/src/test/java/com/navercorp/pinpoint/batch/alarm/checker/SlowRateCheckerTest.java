/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.applicationmap.dao.ApplicationResponse;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlowRateCheckerTest {

    private static final String SERVICE_NAME = "local_service";
    private static final String SERVICE_TYPE = "tomcat";

    private MapResponseDao mockAppMapResponseDAO;

    @BeforeEach
    void before() {
        mockAppMapResponseDAO = mock(MapResponseDao.class);
        when(mockAppMapResponseDAO.selectApplicationResponse(any(), any())).thenAnswer(new Answer<ApplicationResponse>() {
            @Override
            public ApplicationResponse answer(InvocationOnMock invocation) throws Throwable {
                Application application = invocation.getArgument(0, Application.class);
                return testData(application);
            }
        });
    }

    private ApplicationResponse testData(Application application) {
        long timeStamp = 1409814914298L;
        ApplicationResponse.Builder responseTime = ApplicationResponse.newBuilder(application);

        TimeHistogram histogram;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                histogram = new TimeHistogram(ServiceType.STAND_ALONE, timeStamp);
                histogram.addCallCountByElapsedTime(1000, false);
                histogram.addCallCountByElapsedTime(3000, false);
                histogram.addCallCountByElapsedTime(5000, false);
                histogram.addCallCountByElapsedTime(6000, false);
                histogram.addCallCountByElapsedTime(7000, false);
                responseTime.addResponseTime("agent_" + i + "_" + j, timeStamp, histogram);
            }
            timeStamp += 1;
        }

        return responseTime.build();
    }

    /*
     * not satisfied with alert condition
     */
    @Test
    public void checkTest1() {
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);

        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockAppMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.SLOW_RATE.getName(), 60, "testGroup", false, false, false, "");
        SlowRateChecker filter = new SlowRateChecker(collector, rule);

        filter.check();
        assertTrue(filter.isDetected());
    }

    /*
     * not satisfied with alert condition
     */
    @Test
    public void checkTest2() {
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockAppMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.SLOW_RATE.getName(), 61, "testGroup", false, false, false, "");
        SlowRateChecker filter = new SlowRateChecker(collector, rule);

        filter.check();
        assertFalse(filter.isDetected());
    }

}
