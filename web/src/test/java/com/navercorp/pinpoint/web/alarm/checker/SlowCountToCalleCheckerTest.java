package com.nhn.pinpoint.web.alarm.checker;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.alarm.CheckerCategory;
import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkData;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.nhn.pinpoint.web.dao.MapStatisticsCallerDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

public class SlowCountToCalleCheckerTest {
    
    private static final String FROM_SERVICE_NAME = "from_local_service";
    private static final String TO_SERVICE_NAME = "to_local_service";
    public static MapStatisticsCallerDao dao;
    
    @BeforeClass
    public static void before() {
        dao = new MapStatisticsCallerDao() {
            
            @Override
            public List<LinkDataMap> selectCallerStatistics(Application callerApplication, Application calleeApplication, Range range) {
                return null;
            }
            
            @Override
            public LinkDataMap selectCaller(Application callerApplication, Range range) {
                long timeStamp = 1409814914298L;
                LinkDataMap linkDataMap = new LinkDataMap();
                Application fromApplication = new Application(FROM_SERVICE_NAME, ServiceType.TOMCAT);
                for (int i = 1 ; i < 6  ; i++) {
                    LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
                    Application toApplication = new Application(TO_SERVICE_NAME + i, ServiceType.TOMCAT);
                    Collection<TimeHistogram> timeHistogramList = new ArrayList<TimeHistogram>();
                    
                    for (int j = 1 ; j < 11  ; j++) {
                        TimeHistogram timeHistogram = new TimeHistogram(ServiceType.TOMCAT, timeStamp);
                        timeHistogram.addCallCountByElapsedTime(i * j * 1000);
                        timeHistogramList.add(timeHistogram);
                    }
                    
                    linkCallDataMap.addCallData(fromApplication.getName(), fromApplication.getServiceType().getCode(), toApplication.getName(), toApplication.getServiceType().getCode(), timeHistogramList);
                    LinkData linkData = new LinkData(fromApplication, toApplication, linkCallDataMap);
                    linkDataMap.addLinkData(linkData);
                }
                
                return linkDataMap;
            }
        };
    }
    
    @Test
    public void checkTest() {
        Application application = new Application(FROM_SERVICE_NAME, ServiceType.TOMCAT);
        MapStatisticsCallerCollector dataCollector = new MapStatisticsCallerCollector(DataCollectorCategory.CALLER_STAT, application, dao, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(FROM_SERVICE_NAME, CheckerCategory.SLOW_COUNT_TO_CALLE.getName(), 7, "testGroup", false, false, TO_SERVICE_NAME + 1);
        SlowCountToCalleChecker checker = new SlowCountToCalleChecker(dataCollector, rule);
        
        checker.check();
        assertTrue(checker.isDetected());
    }
    
    @Test
    public void checkTest2() {
        Application application = new Application(FROM_SERVICE_NAME, ServiceType.TOMCAT);
        MapStatisticsCallerCollector dataCollector = new MapStatisticsCallerCollector(DataCollectorCategory.CALLER_STAT, application, dao, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(FROM_SERVICE_NAME, CheckerCategory.SLOW_COUNT_TO_CALLE.getName(), 6, "testGroup", false, false, TO_SERVICE_NAME + 1);
        SlowCountToCalleChecker checker = new SlowCountToCalleChecker(dataCollector, rule);
        
        checker.check();
        assertTrue(checker.isDetected());
    }
    
    @Test
    public void checkTest3() {
        Application application = new Application(FROM_SERVICE_NAME, ServiceType.TOMCAT);
        MapStatisticsCallerCollector dataCollector = new MapStatisticsCallerCollector(DataCollectorCategory.CALLER_STAT, application, dao, System.currentTimeMillis(), 300000);
        Rule rule = new Rule(FROM_SERVICE_NAME, CheckerCategory.SLOW_COUNT_TO_CALLE.getName(), 9, "testGroup", false, false, TO_SERVICE_NAME + 2);
        SlowCountToCalleChecker checker = new SlowCountToCalleChecker(dataCollector, rule);
        
        checker.check();
        assertTrue(checker.isDetected());
    }

}
