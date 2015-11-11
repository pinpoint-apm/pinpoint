/*
 * Copyright 2014 NAVER Corp.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.checker.ErrorCountChecker;
import com.navercorp.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseTime;

public class ErrorCountCheckerTest {
    
    private static final String SERVICE_NAME = "local_service";
    private static final String SERVICE_TYPE = "tomcat";
    
    private static MapResponseDao mockMapResponseDAO;
    
    @BeforeClass
    public static void before() {
        mockMapResponseDAO = new MapResponseDao() {
            
            @Override
            public List<ResponseTime> selectResponseTime(Application application, Range range) {
                List<ResponseTime> list = new LinkedList<ResponseTime>();
                long timeStamp = 1409814914298L;
                ResponseTime responseTime = new ResponseTime(SERVICE_NAME, ServiceType.STAND_ALONE, timeStamp);
                list.add(responseTime);
                TimeHistogram histogram = null;

                for (int i=0 ; i < 5; i++) {
                    for (int j=0 ; j < 5; j++) {
                        histogram = new TimeHistogram(ServiceType.STAND_ALONE, timeStamp);
                        histogram.addCallCountByElapsedTime(1000, false);
                        histogram.addCallCountByElapsedTime(3000, false);
                        histogram.addCallCountByElapsedTime(1000, true);
                        histogram.addCallCountByElapsedTime(1000, true);
                        histogram.addCallCountByElapsedTime(1000, true);
                        responseTime.addResponseTime("agent_" + i + "_" + j, histogram);
                    }
                    
                    timeStamp += 1;
                }
                
                return list;
            }
        };
    }

    /*
     * alert conditions not satisfied
     */
    @Test
    public void checkTest1() {
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.ERROR_COUNT.getName(), 74, "testGroup", false, false, "");
        ErrorCountChecker filter = new ErrorCountChecker(collector, rule);
    
        filter.check();
        assertTrue(filter.isDetected());
    }
    
    /*
     * alert conditions not satisfied
     */
    @Test
    public void checkTest2() {
        Application application = new Application(SERVICE_NAME, ServiceType.STAND_ALONE);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, SERVICE_TYPE, CheckerCategory.ERROR_COUNT.getName(), 76, "testGroup", false, false, "");
        ErrorCountChecker filter = new ErrorCountChecker(collector, rule);
    
        filter.check();
        assertFalse(filter.isDetected());
    }
}
