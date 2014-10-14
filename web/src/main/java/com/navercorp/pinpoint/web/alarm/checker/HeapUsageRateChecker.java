package com.nhn.pinpoint.web.alarm.checker;

import java.util.Map;

import com.nhn.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class HeapUsageRateChecker extends AgentChecker {
    
    public HeapUsageRateChecker(AgentStatDataCollector dataCollector, Rule rule) {
        super(rule, "%", dataCollector);
    }

    @Override
    protected Map<String, Long> getAgentValues() {
        return ((AgentStatDataCollector)dataCollector).getHeapUsageRate();
    }
    
}
