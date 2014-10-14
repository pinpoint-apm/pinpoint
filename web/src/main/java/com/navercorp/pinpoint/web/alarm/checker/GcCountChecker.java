package com.nhn.pinpoint.web.alarm.checker;

import java.util.Map;

import com.nhn.pinpoint.web.alarm.collector.AgentStatDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class GcCountChecker extends AgentChecker {
    
    public GcCountChecker(AgentStatDataCollector dataCollector, Rule rule) {
        super(rule, "", dataCollector);
    }

    @Override
    protected Map<String, Long> getAgentValues() {
        return ((AgentStatDataCollector)dataCollector).getGCCount();
    }
}
