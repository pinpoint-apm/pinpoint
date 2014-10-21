package com.nhn.pinpoint.web.alarm.checker;

import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

/**
 * 
 * @author koo.taejin
 */
public class SlowRateChecker extends AlarmChecker {

	public SlowRateChecker(ResponseTimeDataCollector dataCollector, Rule rule) {
        super(rule, "%", dataCollector);
	}

	@Override
    protected long getDetectedValue() {
        return ((ResponseTimeDataCollector)dataCollector).getSlowRate();
    }
}