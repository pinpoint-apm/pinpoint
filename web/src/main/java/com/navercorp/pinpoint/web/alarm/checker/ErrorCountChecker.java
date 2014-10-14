package com.nhn.pinpoint.web.alarm.checker;

import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class ErrorCountChecker extends AlarmChecker {

    public ErrorCountChecker(ResponseTimeDataCollector dataCollector, Rule rule) {
        super(rule, "", dataCollector);
    }

    @Override
    protected long getDetectedValue() {
        return ((ResponseTimeDataCollector)dataCollector).getErrorCount();
    }
}
