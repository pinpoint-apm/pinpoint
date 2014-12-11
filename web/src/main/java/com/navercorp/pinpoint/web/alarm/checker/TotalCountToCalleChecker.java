package com.navercorp.pinpoint.web.alarm.checker;

import com.navercorp.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector;
import com.navercorp.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector.DataCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

/**
 * @author minwoo.jung
 */
public class TotalCountToCalleChecker extends AlarmChecker {

    public TotalCountToCalleChecker(MapStatisticsCallerDataCollector dataCollector, Rule rule) {
        super(rule, "", dataCollector);
    }

    @Override
    protected long getDetectedValue() {
        String calleName = rule.getNotes();
        return ((MapStatisticsCallerDataCollector)dataCollector).getCount(calleName, DataCategory.TOTAL_COUNT);
    }
}
