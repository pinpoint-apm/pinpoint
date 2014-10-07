package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class ErrorRateChecker extends AlarmCheckFilter {
    
    public ErrorRateChecker(ResponseTimeDataCollector dataCollector, Rule rule) {
        super(rule, "%", dataCollector);
    }

    @Override
    protected long getDetectedValue() {
        return ((ResponseTimeDataCollector)dataCollector).getErrorRate();
    }
}
