package com.navercorp.pinpoint.web.alarm.checker;

import com.navercorp.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

/**
 * @author minwoo.jung
 */
public class ResponseCountChecker extends AlarmChecker {
    
    public ResponseCountChecker(ResponseTimeDataCollector dataCollector, Rule rule) {
        super(rule, "", dataCollector);
    }

    @Override
    protected long getDetectedValue() {
        return ((ResponseTimeDataCollector)dataCollector).getTotalCount();
    }
}
