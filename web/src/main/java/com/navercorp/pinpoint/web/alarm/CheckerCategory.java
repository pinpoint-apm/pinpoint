package com.nhn.pinpoint.web.alarm;

import java.util.LinkedList;
import java.util.List;

import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.nhn.pinpoint.web.alarm.checker.AlarmChecker;
import com.nhn.pinpoint.web.alarm.checker.ErrorCountChecker;
import com.nhn.pinpoint.web.alarm.checker.ErrorRateChecker;
import com.nhn.pinpoint.web.alarm.checker.GcCountChecker;
import com.nhn.pinpoint.web.alarm.checker.HeapUsageRateChecker;
import com.nhn.pinpoint.web.alarm.checker.JvmCpuUsageRateChecker;
import com.nhn.pinpoint.web.alarm.checker.ResponseCountChecker;
import com.nhn.pinpoint.web.alarm.checker.SlowCountFilter;
import com.nhn.pinpoint.web.alarm.checker.SlowRatesFilter;
import com.nhn.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public enum CheckerCategory {

    SLOW_COUNT("SLOW_COUNT", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new SlowCountFilter((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    SLOW_RATE("SLOW_RATE", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new SlowRatesFilter((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    ERROR_COUNT("ERROR_COUNT", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new ErrorCountChecker((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    ERROR_RATE("ERROR_RATE", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new ErrorRateChecker((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    RESPONSE_COUNT("RESPONSE_COUNT", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new ResponseCountChecker((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    HEAP_USAGE_RATE("HEAP_USAGE_RATE", DataCollectorCategory.AGENT_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new HeapUsageRateChecker((AgentStatDataCollector)dataCollector, rule);
        }
    },
    
    GC_COUNT("GC_COUNT", DataCollectorCategory.AGENT_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new GcCountChecker((AgentStatDataCollector)dataCollector, rule);
        }
    },
    
    JVM_CPU_USAGE_RATE("JVM_CPU_USAGE_RATE", DataCollectorCategory.AGENT_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new JvmCpuUsageRateChecker((AgentStatDataCollector)dataCollector, rule);
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
    
    public abstract AlarmChecker createChecker(DataCollector dataCollector, Rule rule);

}
