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

package com.navercorp.pinpoint.web.alarm;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.navercorp.pinpoint.web.dao.AlarmDao;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.alarm.collector.DataCollector;
import com.navercorp.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.service.AlarmServiceImpl;
import com.navercorp.pinpoint.web.vo.Application;

public class AlarmReaderTest {

    private static ApplicationIndexDao applicationIndexDao;
    private static AlarmService alarmService;
    private static DataCollectorFactory dataCollectorFactory;
    private static final String APP_NAME = "app";
    private static final String SERVICE_TYPE = "tomcat";
    
    @Test
    public void readTest() {
        StepExecution stepExecution = new StepExecution("alarmStep", null);
        ExecutionContext executionContext = new ExecutionContext();
        stepExecution.setExecutionContext(executionContext);

        AlarmReader reader = new AlarmReader(dataCollectorFactory, applicationIndexDao, alarmService);

        reader.beforeStep(stepExecution);

        for(int i = 0; i < 7; i++) {
            assertNotNull(reader.read());
        }
        
        assertNull(reader.read());
    }

    @Test
    public void readTest3() {
        StepExecution stepExecution = new StepExecution("alarmStep", null);
        ExecutionContext executionContext = new ExecutionContext();
        stepExecution.setExecutionContext(executionContext);
        
        AlarmServiceImpl alarmService = new AlarmServiceImpl(mock(AlarmDao.class)) {
            @Override
            public java.util.List<Rule> selectRuleByApplicationId(String applicationId) {
                return new LinkedList<>();
            }
        };
        
        AlarmReader reader = new AlarmReader(dataCollectorFactory, applicationIndexDao, alarmService);
        reader.beforeStep(stepExecution);
        assertNull(reader.read());
    }
    
    @BeforeClass
    public static void beforeClass() {
        applicationIndexDao = new ApplicationIndexDao() {

            @Override
            public List<Application> selectAllApplicationNames() {
                List<Application> apps = new LinkedList<Application>();

                for(int i = 0; i < 7; i++) {
                    apps.add(new Application(APP_NAME + i, ServiceType.STAND_ALONE));
                }
                return apps;
            }

            @Override public List<String> selectAgentIds(String applicationName) {return null;}
            @Override public void deleteApplicationName(String applicationName) { }

            @Override
            public void deleteAgentIds(Map<String, List<String>> applicationAgentIdMap) {}

            @Override public void deleteAgentId(String applicationName, String agentId) {}
            
        };

        alarmService = new AlarmServiceImpl(mock(AlarmDao.class)) {
            private Map<String, Rule> ruleMap ;

            {
                ruleMap = new HashMap<String, Rule>();

                for(int i = 0; i <=6; i++) {
                    ruleMap.put(APP_NAME + i, new Rule(APP_NAME + i, SERVICE_TYPE, CheckerCategory.SLOW_COUNT.getName(), 76, "testGroup", false, false, ""));
                }
            }

            @Override
            public List<Rule> selectRuleByApplicationId(String applicationId) {
                List<Rule> rules = new LinkedList<>();
                rules.add(ruleMap.get(applicationId));
                return rules;
            }
        };
        
        dataCollectorFactory = new DataCollectorFactory() {
            @Override
            public DataCollector createDataCollector(CheckerCategory checker, Application application, long timeSlotEndTime) {
                return new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, null, null, 0, 0);
            }
        };
    }
}
