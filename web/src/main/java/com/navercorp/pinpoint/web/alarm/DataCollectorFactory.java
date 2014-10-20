package com.nhn.pinpoint.web.alarm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerCollector;
import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.dao.hbase.HbaseAgentStatDao;
import com.nhn.pinpoint.web.dao.hbase.HbaseApplicationIndexDao;
import com.nhn.pinpoint.web.dao.hbase.HbaseMapResponseTimeDao;
import com.nhn.pinpoint.web.dao.hbase.HbaseMapStatisticsCallerDao;
import com.nhn.pinpoint.web.vo.Application;

@Component
public class DataCollectorFactory {
    
    public final static long SLOT_INTERVAL_FIVE_MIN = 300000;
    
    public final static long SLOT_INTERVAL_THREE_MIN = 180000;

    @Autowired
    private HbaseMapResponseTimeDao hbaseMapResponseTimeDao;
    
    @Autowired
    private HbaseAgentStatDao hbaseAgentStatDao;
    
    @Autowired
    private HbaseApplicationIndexDao hbaseApplicationIndexDao;
    
    @Autowired
    private HbaseMapStatisticsCallerDao mapStatisticsCallerDao;
    
    public DataCollector createDataCollector(CheckerCategory checker, Application application, long timeSlotEndTime) {
        switch (checker.getDataCollectorCategory()) {
        case RESPONSE_TIME:
            return new ResponseTimeDataCollector(DataCollectorCategory.RESPONSE_TIME, application, hbaseMapResponseTimeDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
        case AGENT_STAT:
            return new AgentStatDataCollector(DataCollectorCategory.AGENT_STAT, application, hbaseAgentStatDao, hbaseApplicationIndexDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
        case CALLER_STAT:
            return new MapStatisticsCallerCollector(DataCollectorCategory.CALLER_STAT, application, mapStatisticsCallerDao, timeSlotEndTime, SLOT_INTERVAL_FIVE_MIN);
        }
        
        throw new IllegalArgumentException("not create DataCollector : " + checker.getName());
        
    }
    
    public enum DataCollectorCategory {
        RESPONSE_TIME,
        AGENT_STAT,
        CALLER_STAT;
    }
}
