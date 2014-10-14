package com.nhn.pinpoint.web.alarm.checker;

import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class ErrorRateChecker extends AlarmChecker {
    
    public ErrorRateChecker(ResponseTimeDataCollector dataCollector, Rule rule) {
        super(rule, "%", dataCollector);
    }

    @Override
    protected long getDetectedValue() {
        return ((ResponseTimeDataCollector)dataCollector).getErrorRate();
    }
}
