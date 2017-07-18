/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.DataCollectorFactory.DataCollectorCategory;
import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.checker.DataSourceConnectionUsageRateChecker;
import com.navercorp.pinpoint.web.alarm.checker.DeadlockChecker;
import com.navercorp.pinpoint.web.alarm.checker.ErrorCountChecker;
import com.navercorp.pinpoint.web.alarm.checker.ErrorCountToCalleeChecker;
import com.navercorp.pinpoint.web.alarm.checker.ErrorRateChecker;
import com.navercorp.pinpoint.web.alarm.checker.ErrorRateToCalleeChecker;
import com.navercorp.pinpoint.web.alarm.checker.HeapUsageRateChecker;
import com.navercorp.pinpoint.web.alarm.checker.JvmCpuUsageRateChecker;
import com.navercorp.pinpoint.web.alarm.checker.ResponseCountChecker;
import com.navercorp.pinpoint.web.alarm.checker.SlowCountChecker;
import com.navercorp.pinpoint.web.alarm.checker.SlowCountToCalleeChecker;
import com.navercorp.pinpoint.web.alarm.checker.SlowRateChecker;
import com.navercorp.pinpoint.web.alarm.checker.SlowRateToCalleeChecker;
import com.navercorp.pinpoint.web.alarm.checker.TotalCountToCalleeChecker;
import com.navercorp.pinpoint.web.alarm.collector.AgentEventDataCollector;
import com.navercorp.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.navercorp.pinpoint.web.alarm.collector.DataCollector;
import com.navercorp.pinpoint.web.alarm.collector.DataSourceDataCollector;
import com.navercorp.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector;
import com.navercorp.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author minwoo.jung
 */
public enum CheckerCategory {

    SLOW_COUNT("SLOW COUNT", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new SlowCountChecker((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    SLOW_RATE("SLOW RATE", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new SlowRateChecker((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    ERROR_COUNT("ERROR COUNT", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new ErrorCountChecker((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    ERROR_RATE("ERROR RATE", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new ErrorRateChecker((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    TOTAL_COUNT("TOTAL COUNT", DataCollectorCategory.RESPONSE_TIME) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new ResponseCountChecker((ResponseTimeDataCollector)dataCollector, rule);
        }
    },
    
    SLOW_COUNT_TO_CALLEE("SLOW COUNT TO CALLEE", DataCollectorCategory.CALLER_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new SlowCountToCalleeChecker((MapStatisticsCallerDataCollector)dataCollector, rule);
        }
    },
    
    SLOW_RATE_TO_CALLEE("SLOW RATE TO CALLEE", DataCollectorCategory.CALLER_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new SlowRateToCalleeChecker((MapStatisticsCallerDataCollector)dataCollector, rule);
        }
    },
    
    ERROR_COUNT_TO_CALLEE("ERROR COUNT TO CALLEE", DataCollectorCategory.CALLER_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new ErrorCountToCalleeChecker((MapStatisticsCallerDataCollector)dataCollector, rule);
        }
    },
    
    ERROR_RATE_TO_CALLEE("ERROR RATE TO CALLEE", DataCollectorCategory.CALLER_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new ErrorRateToCalleeChecker((MapStatisticsCallerDataCollector)dataCollector, rule);
        }
    },
    
    TOTAL_COUNT_TO_CALLEE("TOTAL COUNT TO CALLEE", DataCollectorCategory.CALLER_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new TotalCountToCalleeChecker((MapStatisticsCallerDataCollector)dataCollector, rule);
        }
    },
    
    HEAP_USAGE_RATE("HEAP USAGE RATE", DataCollectorCategory.AGENT_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new HeapUsageRateChecker((AgentStatDataCollector)dataCollector, rule);
        }
    },
    
//    GC_COUNT("GC COUNT", DataCollectorCategory.AGENT_STAT) {
//        @Override
//        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
//            return new GcCountChecker((AgentStatDataCollector)dataCollector, rule);
//        }
//    },
    
    JVM_CPU_USAGE_RATE("JVM CPU USAGE RATE", DataCollectorCategory.AGENT_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new JvmCpuUsageRateChecker((AgentStatDataCollector)dataCollector, rule);
        }
    },

    DATASOURCE_CONNECTION_USAGE_RATE("DATASOURCE CONNECTION USAGE RATE", DataCollectorCategory.DATA_SOURCE_STAT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new DataSourceConnectionUsageRateChecker((DataSourceDataCollector) dataCollector, rule);
        }
    },
    DEADLOCK_OCCURRENCE("DEADLOCK OCCURRENCE", DataCollectorCategory.AGENT_EVENT) {
        @Override
        public AlarmChecker createChecker(DataCollector dataCollector, Rule rule) {
            return new DeadlockChecker((AgentEventDataCollector) dataCollector, rule);
        }
    };
    private static final Set<CheckerCategory> CHECKER_CATEGORIES = EnumSet.allOf(CheckerCategory.class);

    
    public static CheckerCategory getValue(String value) {
        for (CheckerCategory category : CHECKER_CATEGORIES) {
            if (category.getName().equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown CheckerCategory : " + value);
    }

    public static List<String> getNames() {

        final List<String> names = new ArrayList<>(CHECKER_CATEGORIES.size());
        for (CheckerCategory category : CHECKER_CATEGORIES) {
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
