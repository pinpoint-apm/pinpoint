package com.nhn.pinpoint.web.alarm.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.alarm.CheckerCategory;
import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.nhn.pinpoint.web.alarm.checker.SlowCountFilter;
import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.dao.MapResponseDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.ResponseTime;

public class SlowCountFilterTest {
    
    private static final String SERVICE_NAME = "local_service"; 
    
    private static MapResponseDao mockMapResponseDAO;
    
    @BeforeClass
    public static void before() {
        mockMapResponseDAO = new MapResponseDao() {
            
            @Override
            public List<ResponseTime> selectResponseTime(Application application, Range range) {
                List<ResponseTime> list = new LinkedList<ResponseTime>();
                long timeStamp = 1409814914298L;
                ResponseTime responseTime = new ResponseTime(SERVICE_NAME, ServiceType.TOMCAT.getCode(), timeStamp);
                list.add(responseTime);
                TimeHistogram histogram = null;

                for (int i=0 ; i < 5; i++) {
                    for (int j=0 ; j < 5; j++) {
                        histogram = new TimeHistogram(ServiceType.TOMCAT, timeStamp);
                        histogram.addCallCountByElapsedTime(1000);
                        histogram.addCallCountByElapsedTime(3000);
                        histogram.addCallCountByElapsedTime(5000);
                        histogram.addCallCountByElapsedTime(6000);
                        histogram.addCallCountByElapsedTime(7000);
                        responseTime.addResponseTime("agent_" + i + "_" + j, histogram);
                    }
                    
                    timeStamp += 1;
                }
                
                return list;
            }
        };
    }

    /*
     * 알람 조건 만족함
     */
    @Test
    public void checkTest1() {
        Application application = new Application(SERVICE_NAME, ServiceType.TOMCAT);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, CheckerCategory.SLOW_COUNT.getName(), 74, "testGroup", false, false);
        SlowCountFilter filter = new SlowCountFilter(collector, rule);
    
        filter.check();
        assertTrue(filter.isDetected());
    }
    
    /*
     * 알람 조건 만족못함.
     */
    @Test
    public void checkTest2() {
        Application application = new Application(SERVICE_NAME, ServiceType.TOMCAT);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, mockMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, CheckerCategory.SLOW_COUNT.getName(), 76, "testGroup", false, false);
        SlowCountFilter filter = new SlowCountFilter(collector, rule);
    
        filter.check();
        assertFalse(filter.isDetected());
    }
    
    /*
        직접 Nbase 서버에 접속해서 데이터 가져와서 테스트
        계속 데이터가 보존되있지 않으므로 필요시 사용
    */
    /*
    @Autowired
    private HbaseMapResponseTimeDao hbaseMapResponseTimeDao;
    
    @Test
    public void checkTest1() {
        Application application = new Application(SERVICE_NAME, ServiceType.TOMCAT);
        SlowCountFilter filter = new SlowCountFilter(application);
        
        AlarmRuleResource rule = new AlarmRuleResource();
        rule.setThresholdRule(10);
        rule.setContinuosTime(36000000);
        filter.initialize(rule);
        
        DefaultAlarmEvent event = new DefaultAlarmEvent(System.currentTimeMillis(), hbaseMapResponseTimeDao);
        assertTrue(filter.check(event));
    }
    */
}
