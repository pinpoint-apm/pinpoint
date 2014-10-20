package com.nhn.pinpoint.web.alarm.collector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkCallData;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkData;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.nhn.pinpoint.web.dao.MapStatisticsCallerDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

public class MapStatisticsCallerCollector extends DataCollector {

    private Application application;
    private MapStatisticsCallerDao mapStatisticsCallerDao; 
    private long timeSlotEndTime;
    private long slotInterval;
    private Map<String, LinkCallData> calleStatMap = new HashMap<String, LinkCallData>();
    private final AtomicBoolean init =new AtomicBoolean(false);// 동시에 checker들이 동작 되면 동시성 고려가 필요함
    
    public MapStatisticsCallerCollector(DataCollectorCategory category, Application application, MapStatisticsCallerDao mapStatisticsCallerDao, long timeSlotEndTime, long slotInterval) {
        super(category);
        this.application = application;
        this.mapStatisticsCallerDao = mapStatisticsCallerDao;
        this.timeSlotEndTime = timeSlotEndTime;
        this.slotInterval = slotInterval; 
    }

    @Override
    public void collect() {
        if (init.get()) {
            return;
        }
        
        LinkDataMap callerDataMap = mapStatisticsCallerDao.selectCaller(application, new Range(timeSlotEndTime - slotInterval, timeSlotEndTime));

        for (LinkData linkData : callerDataMap.getLinkDataList()) {
            LinkCallDataMap linkCallDataMap = linkData.getLinkCallDataMap();
       
            for (LinkCallData linkCallData : linkCallDataMap.getLinkDataList()) {
                calleStatMap.put(linkCallData.getTarget(), linkCallData);
            }
        }
        
        init.set(true);
    }

    public long getCount(String calleName, DataCategory dataCategory) {
        LinkCallData linkCallData = calleStatMap.get(calleName);
        long count = 0;
        
        if (linkCallData != null) {
            switch (dataCategory) {
            case SLOW_COUNT:
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    count += timeHistogram.getSlowCount();
                    count += timeHistogram.getVerySlowCount();
                }
                break;
            case ERROR_COUNT:
                for (TimeHistogram timeHistogram : linkCallData.getTimeHistogram()) {
                    count += timeHistogram.getErrorCount();
                }
                break;
            }
            
            
            return count;
        }
        
        return 0;
    }

    public enum DataCategory {
        SLOW_COUNT, 
        ERROR_COUNT;
    }
}
