package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class ResponseCountChecker extends AlarmCheckFilter {
    
    public ResponseCountChecker(ResponseTimeDataCollector dataCollector, Rule rule) {
        super(rule, "", dataCollector);
    }

    @Override
    protected long getDetectedValue() {
        return ((ResponseTimeDataCollector)dataCollector).getTotalCount();
    }
}
