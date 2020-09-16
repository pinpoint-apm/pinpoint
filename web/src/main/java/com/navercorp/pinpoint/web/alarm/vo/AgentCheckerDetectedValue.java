package com.navercorp.pinpoint.web.alarm.vo;

import java.util.Map;

public class AgentCheckerDetectedValue<T> extends CheckerDetectedValue {
    
    private Map<String, T> agentValues;
    
    public AgentCheckerDetectedValue(String unit, Map<String, T> agentValues) {
        super(unit);
        this.agentValues = agentValues;
    }
    
    public Map<String, T> getAgentValues() {
        return agentValues;
    }
}
