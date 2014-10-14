package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public abstract class AlarmCheckCountFilter extends AlarmCheckFilter {

    protected AlarmCheckCountFilter(Rule rule, String unit, DataCollector dataCollector) {
        super(rule, unit, dataCollector);
    }
}
