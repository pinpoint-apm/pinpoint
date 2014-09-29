package com.nhn.pinpoint.web.alarm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.dao.hbase.HbaseMapResponseTimeDao;
import com.nhn.pinpoint.web.vo.Application;

@Component
public class DataCollectorFactory {

    @Autowired
    private HbaseMapResponseTimeDao hbaseMapResponseTimeDao;
    
    public DataCollector createDataCollector(CheckerCategory checker, Application application, long timeSlotEndTime, long slotInterval) {
        switch (checker.getDataCollectorCategory()) {
        case RESPONSE_TIME:
            return new ResponseTimeDataCollector(application, hbaseMapResponseTimeDao, timeSlotEndTime, slotInterval);
        }
        
        throw new RuntimeException("not create DataCollector : " + checker.getName());
        
    }
    
    public enum DataCollectorCategory {
        RESPONSE_TIME;
    }
}
