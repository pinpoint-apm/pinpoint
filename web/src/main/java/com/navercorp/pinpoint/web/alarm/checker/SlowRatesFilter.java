package com.nhn.pinpoint.web.alarm.checker;

import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

/**
 * 
 * @author koo.taejin
 */
public class SlowRatesFilter extends AlarmChecker {

	public SlowRatesFilter(ResponseTimeDataCollector dataCollector, Rule rule) {
        super(rule, "%", dataCollector);
	}

	@Override
    protected long getDetectedValue() {
        return ((ResponseTimeDataCollector)dataCollector).getSlowRate();
    }
}