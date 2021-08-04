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

package com.navercorp.pinpoint.batch.alarm;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author minwoo.jung
 */
public enum CheckerCategory {

    SLOW_COUNT("SLOW COUNT", DataCollectorCategory.RESPONSE_TIME),
    
    SLOW_RATE("SLOW RATE", DataCollectorCategory.RESPONSE_TIME),
    
    ERROR_COUNT("ERROR COUNT", DataCollectorCategory.RESPONSE_TIME),
    
    ERROR_RATE("ERROR RATE", DataCollectorCategory.RESPONSE_TIME),
    
    TOTAL_COUNT("TOTAL COUNT", DataCollectorCategory.RESPONSE_TIME),
    
    SLOW_COUNT_TO_CALLEE("SLOW COUNT TO CALLEE", DataCollectorCategory.CALLER_STAT),
    
    SLOW_RATE_TO_CALLEE("SLOW RATE TO CALLEE", DataCollectorCategory.CALLER_STAT),
    
    ERROR_COUNT_TO_CALLEE("ERROR COUNT TO CALLEE", DataCollectorCategory.CALLER_STAT),
    
    ERROR_RATE_TO_CALLEE("ERROR RATE TO CALLEE", DataCollectorCategory.CALLER_STAT),
    
    TOTAL_COUNT_TO_CALLEE("TOTAL COUNT TO CALLEE", DataCollectorCategory.CALLER_STAT),
    
    HEAP_USAGE_RATE("HEAP USAGE RATE", DataCollectorCategory.AGENT_STAT),
    
//    GC_COUNT("GC COUNT", DataCollectorCategory.AGENT_STAT),
    
    JVM_CPU_USAGE_RATE("JVM CPU USAGE RATE", DataCollectorCategory.AGENT_STAT),

    SYSTEM_CPU_USAGE_RATE("SYSTEM CPU USAGE RATE", DataCollectorCategory.AGENT_STAT),

    DATASOURCE_CONNECTION_USAGE_RATE("DATASOURCE CONNECTION USAGE RATE", DataCollectorCategory.DATA_SOURCE_STAT),

    DEADLOCK_OCCURRENCE("DEADLOCK OCCURRENCE", DataCollectorCategory.AGENT_EVENT),

    FILE_DESCRIPTOR_COUNT("FILE DESCRIPTOR COUNT", DataCollectorCategory.FILE_DESCRIPTOR);

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
}
