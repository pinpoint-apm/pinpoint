package com.navercorp.pinpoint.web.alarm.checker;

import com.navercorp.pinpoint.web.alarm.collector.DataCollector;
import com.navercorp.pinpoint.web.alarm.vo.AgentCheckerDetectedValue;
import com.navercorp.pinpoint.web.alarm.vo.CheckerDetectedValue;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

import java.util.Map;

public abstract class BooleanValueAgentChecker extends AgentChecker<Boolean>{
    
    protected BooleanValueAgentChecker(Rule rule, String unit, DataCollector dataCollector) {
        super(rule, unit, dataCollector);
    }
    
    @Override
    public String getCheckerType() {
        return BooleanValueAgentChecker.class.getSimpleName();
    }
    
    protected abstract Map<String, Boolean> getAgentValues();
    
}
