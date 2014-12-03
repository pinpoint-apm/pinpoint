package com.nhn.pinpoint.web.alarm.collector;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkData;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.nhn.pinpoint.web.dao.hbase.HbaseMapStatisticsCallerDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class MapStatisticsCallerCollectorIT {
    
    @Autowired
    HbaseMapStatisticsCallerDao callerDao;
    
    @Test
    public void test() {
        Application application = new Application("API.GATEWAY.DEV", ServiceType.TOMCAT);
        long current = System.currentTimeMillis();
        Range range = new Range(current - 300000, current);
        LinkDataMap map = callerDao.selectCaller(application, range);

        for (LinkData linkData : map.getLinkDataList()) {
            System.out.println(linkData.getFromApplication() + " : " + linkData.getToApplication() );
            
            LinkCallDataMap linkCallDataMap = linkData.getLinkCallDataMap();
            for (LinkCallData linkCallData : linkCallDataMap.getLinkDataList()) {
                System.out.println("\t"+ linkCallData.getSource() + " : " + linkCallData.getTarget());
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    System.out.println("\t\t" + timeHistogram);
                }
            }
            
            System.out.println(linkData.getLinkCallDataMap());
        }
    }
    
}
