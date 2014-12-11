package com.navercorp.pinpoint.web.alarm.collector;

import com.navercorp.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;

/**
 * @author minwoo.jung
 */
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
