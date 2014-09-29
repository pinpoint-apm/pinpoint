package com.nhn.pinpoint.web.alarm;

import java.util.LinkedList;
import java.util.List;

import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;
import com.nhn.pinpoint.web.alarm.filter.SlowCountFilter;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public enum CheckerCategory {

    SLOW_COUNT("SLOW_COUNT", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmCheckFilter createChecker(DataCollector dataCollector, Rule rule) {
            AlarmCheckFilter filter = new SlowCountFilter(dataCollector, rule);
            return filter;
        }
    };
    
    public static CheckerCategory getValue(String value) {
        for (CheckerCategory category : CheckerCategory.values()) {
            if (category.getName().equalsIgnoreCase(value)) {
                return category;
            }
        }
        
        return null;
    }
    
    public static List<String> getNames() {
        List<String> names = new LinkedList<String>();
        
        for (CheckerCategory category : CheckerCategory.values()) {
            names.add(category.getName());
        }
        
        return names;
    }

    private final String name;
    private final DataCollectorCategory dataCollectorCategory;

    CheckerCategory(String name, DataCollectorCategory dataCollectorCategory) {
        this.name = name;
        this.dataCollectorCategory = dataCollectorCategory;
    }
    
    public DataCollectorCategory getDataCollectorCategory() {
        return this.dataCollectorCategory; 
    }
    
    public String getName() {
        return name;
    }
    
    public abstract AlarmCheckFilter createChecker(DataCollector dataCollector, Rule rule);

}
