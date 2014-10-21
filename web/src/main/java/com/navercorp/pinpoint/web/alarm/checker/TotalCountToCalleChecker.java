package com.nhn.pinpoint.web.alarm.checker;

import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector;
import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector.DataCategory;
import com.nhn.pinpoint.web.alarm.vo.Rule;

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
