package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.collector.ResponseTimeDataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;

/**
 * 
 * @author koo.taejin
 */
public class SlowRatesFilter extends AlarmCheckFilter {

	public SlowRatesFilter(ResponseTimeDataCollector dataCollector, Rule rule) {
        super(rule, "%", dataCollector);
	}

	@Override
    protected long getDetectedValue() {
        return ((ResponseTimeDataCollector)dataCollector).getSlowRate();
    }
}