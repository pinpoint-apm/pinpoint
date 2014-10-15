package com.nhn.pinpoint.web.alarm;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.dao.AlarmResourceDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.dao.mysql.MySqlAlarmResourceDao;
import com.nhn.pinpoint.web.vo.Application;

public class ReaderTest {

    private static ApplicationIndexDao applicationIndexDao;
    private static AlarmResourceDao alarmResourceDao;
    private static DataCollectorFactory dataCollectorFactory;
    private static final String APP_NAME = "app";
    
    @Test
    public void readTest() {
        StepExecution stepExecution = new StepExecution("alarmStep", null);
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.put(AlarmPartitioner.PARTITION_NUMBER, 1);
        stepExecution.setExecutionContext(executionContext);
        
        AlarmReader reader = new AlarmReader(dataCollectorFactory, applicationIndexDao, alarmResourceDao);

        reader.beforeStep(stepExecution);

        for(int i = 0; i < 5; i++) {
            assertNotNull(reader.read());
        }
        
        assertNull(reader.read());
    }
    
    @Test
    public void readTest2() {
        StepExecution stepExecution = new StepExecution("alarmStep", null);
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.put(AlarmPartitioner.PARTITION_NUMBER, 2);
        stepExecution.setExecutionContext(executionContext);
        
        AlarmReader reader = new AlarmReader(dataCollectorFactory, applicationIndexDao, alarmResourceDao);

        reader.beforeStep(stepExecution);

        for(int i = 0; i < 2; i++) {
            assertNotNull(reader.read());
        }
        
        assertNull(reader.read());
    }
    
    @Test
    public void readTest3() {
        StepExecution stepExecution = new StepExecution("alarmStep", null);
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.put(AlarmPartitioner.PARTITION_NUMBER, 2);
        stepExecution.setExecutionContext(executionContext);
        
        MySqlAlarmResourceDao alarmResourceDao = new MySqlAlarmResourceDao() {
            @Override
            public java.util.List<Rule> selectAppRule(String applicationName) {
                return new LinkedList<Rule>();
            }
        };
        
        AlarmReader reader = new AlarmReader(dataCollectorFactory, applicationIndexDao, alarmResourceDao);
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
                    apps.add(new Application(APP_NAME + i, ServiceType.TOMCAT));
                }
                return apps;
            }

            @Override public List<String> selectAgentIds(String applicationName) {return null;}
            @Override public void deleteApplicationName(String applicationName) { }
            
        };
        
        alarmResourceDao = new MySqlAlarmResourceDao() {
            private Map<String, Rule> ruleMap ;
            
            {
                ruleMap = new HashMap<String, Rule>();

                for(int i = 0; i <=6; i++) {
                    ruleMap.put(APP_NAME + i, new Rule(APP_NAME + i, CheckerCategory.SLOW_COUNT.getName(), 76, "testGroup", false, false));
                }
            }
            
            @Override
            public java.util.List<Rule> selectAppRule(String applicationName) {
                List<Rule> rules = new LinkedList<Rule>();
                rules.add(ruleMap.get(applicationName));
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
