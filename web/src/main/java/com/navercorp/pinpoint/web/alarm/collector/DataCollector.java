package com.nhn.pinpoint.web.alarm.collector;

import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;

public abstract class DataCollector {
    
    protected final DataCollectorCategory dataCollectorCategory;
    
    public DataCollector(DataCollectorCategory dataCollectorCategory) {
        this.dataCollectorCategory = dataCollectorCategory;
    }
    
    public abstract void collect();
    
    public DataCollectorCategory getDataCollectorCategory() {
        return dataCollectorCategory;
    }

}
