package com.navercorp.pinpoint.web.alarm.checker;

import com.navercorp.pinpoint.web.alarm.collector.DataCollector;
import com.navercorp.pinpoint.web.alarm.vo.AgentCheckerDetectedValue;
import com.navercorp.pinpoint.web.alarm.vo.CheckerDetectedValue;
import com.navercorp.pinpoint.web.alarm.vo.DataSourceAlarmVO;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

import java.util.List;
import java.util.Map;

public abstract class DataSourceAlarmListValueAgentChecker extends AgentChecker<List<DataSourceAlarmVO>> {
    
    protected DataSourceAlarmListValueAgentChecker(Rule rule, String unit, DataCollector dataCollector) {
        super(rule, unit, dataCollector);
    }
    
    @Override
    public String getCheckerType() {
        return DataSourceAlarmListValueAgentChecker.class.getSimpleName();
    }
    
    protected abstract Map<String, List<DataSourceAlarmVO>> getAgentValues();
    
}
