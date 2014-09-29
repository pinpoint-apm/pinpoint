package com.nhn.pinpoint.web.alarm.filter;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.alarm.CheckerCategory;
import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.dao.MapResponseDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.ResponseTime;

public class ErrorRateCheckerTest {
    
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
                        histogram.addCallCountByElapsedTime(-1);
                        histogram.addCallCountByElapsedTime(-1);
                        histogram.addCallCountByElapsedTime(-1);
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
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(application, mockMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, CheckerCategory.ERROR_RATE.getName(), 60, "testGroup", false, false);
        ErrorRateChecker filter = new ErrorRateChecker(collector, rule);
    
        filter.check();
        assertTrue(filter.isDetected());
    }
    
    /*
     * 알람 조건 만족못함.
     */
    @Test
    public void checkTest2() {
        Application application = new Application(SERVICE_NAME, ServiceType.TOMCAT);
        ResponseTimeDataCollector collector = new ResponseTimeDataCollector(application, mockMapResponseDAO, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(SERVICE_NAME, CheckerCategory.ERROR_RATE.getName(), 61, "testGroup", false, false);
        ErrorRateChecker filter = new ErrorRateChecker(collector, rule);
    
        filter.check();
        assertFalse(filter.isDetected());
    }
}
