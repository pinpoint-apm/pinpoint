package com.nhn.pinpoint.web.alarm;

import java.util.LinkedList;
import java.util.List;

import com.nhn.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.nhn.pinpoint.web.alarm.checker.AlarmChecker;
import com.nhn.pinpoint.web.alarm.checker.ErrorCountChecker;
import com.nhn.pinpoint.web.alarm.checker.ErrorCountToCalleChecker;
import com.nhn.pinpoint.web.alarm.checker.ErrorRateChecker;
import com.nhn.pinpoint.web.alarm.checker.ErrorRateToCalleChecker;
import com.nhn.pinpoint.web.alarm.checker.GcCountChecker;
import com.nhn.pinpoint.web.alarm.checker.HeapUsageRateChecker;
import com.nhn.pinpoint.web.alarm.checker.JvmCpuUsageRateChecker;
import com.nhn.pinpoint.web.alarm.checker.ResponseCountChecker;
import com.nhn.pinpoint.web.alarm.checker.SlowCountChecker;
import com.nhn.pinpoint.web.alarm.checker.SlowCountToCalleChecker;
import com.nhn.pinpoint.web.alarm.checker.SlowRateChecker;
import com.nhn.pinpoint.web.alarm.checker.SlowRateToCalleChecker;
import com.nhn.pinpoint.web.alarm.checker.TotalCountToCalleChecker;
import com.nhn.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector;
import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public enum CheckerCategory {

    SLOW_COUNT("SLOW_COUNT", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new SlowCountChecker((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    SLOW_RATE("SLOW_RATE", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new SlowRateChecker((ResponseTimeDataCollector)dataCollector, rule);
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
    
    SLOW_COUNT_TO_CALLE("SLOW_COUNT_TO_CALLE", DataCollectorCategory.CALLER_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new SlowCountToCalleChecker((MapStatisticsCallerDataCollector)dataCollector, rule);
        }
    },
    
    SLOW_RATE_TO_CALLE("SLOW_RATE_TO_CALLE", DataCollectorCategory.CALLER_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new SlowRateToCalleChecker((MapStatisticsCallerDataCollector)dataCollector, rule);
        }
    },
    
    ERROR_COUNT_TO_CALLE("ERROR_COUNT_TO_CALLE", DataCollectorCategory.CALLER_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new ErrorCountToCalleChecker((MapStatisticsCallerDataCollector)dataCollector, rule);
        }
    },
    
    ERROR_RATE_TO_CALLE("ERROR_RATE_TO_CALLE", DataCollectorCategory.CALLER_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new ErrorRateToCalleChecker((MapStatisticsCallerDataCollector)dataCollector, rule);
        }
    },
    
    TOTAL_COUNT_TO_CALLE("TOTAL_COUNT_TO_CALLE", DataCollectorCategory.CALLER_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new TotalCountToCalleChecker((MapStatisticsCallerDataCollector)dataCollector, rule);
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
