/*
 * Copyright 2022 NAVER Corp.
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

import com.navercorp.pinpoint.batch.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author youngjin.kim2
 */
public class ApdexScoreCheckerTest {

    private static final String SERVICE_NAME = "local_service";
    private static final String SERVICE_TYPE = "tomcat";

    private static MapResponseDao mockMapResponseDAO;

    @BeforeAll
    public static void before() {
        mockMapResponseDAO = (application, range) -> {
            long timeStamp = 1409814914298L;
            ResponseTime responseTime = new ResponseTime(SERVICE_NAME, ServiceType.STAND_ALONE, timeStamp);
            List<ResponseTime> list = List.of(responseTime);

            for (int i=0 ; i < 5; i++) {
                for (int j=0 ; j < 5; j++) {
                    TimeHistogram histogram = new TimeHistogram(ServiceType.STAND_ALONE, timeStamp);
                    histogram.addCallCountByElapsedTime(1000, false);
                    histogram.addCallCountByElapsedTime(1000, false);
                    histogram.addCallCountByElapsedTime(1000, false);
                    histogram.addCallCountByElapsedTime(1000, false);
                    histogram.addCallCountByElapsedTime(6000, false);
                    responseTime.addResponseTime("agent_" + i + "_" + j, histogram);
                }
                timeStamp += 1;
            }

            return list;
        };
    }

    /*
     * alert conditions not satisfied
     */
    @Test
    public void checkTest1() {
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.SLOW_COUNT.getName(), 90, "testGroup", false, false, false, "");
        ApdexScoreChecker checker = new ApdexScoreChecker(collector, rule);

        checker.check();
        Assertions.assertTrue(checker.isDetected());
    }

    /*
     * alert conditions not satisfied
     */
    @Test
    public void checkTest2() {
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.SLOW_COUNT.getName(), 75, "testGroup", false, false, false, "");
        ApdexScoreChecker checker = new ApdexScoreChecker(collector, rule);

        checker.check();
        Assertions.assertFalse(checker.isDetected());
    }

}
