package com.nhn.pinpoint.web.alarm.checker;

import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerCollector;
import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerCollector.DataCategory;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class TotalCountToCalleChecker extends AlarmChecker {

    public TotalCountToCalleChecker(MapStatisticsCallerCollector dataCollector, Rule rule) {
        super(rule, "", dataCollector);
    }

    @Override
    protected long getDetectedValue() {
        String calleName = rule.getNotes();
        return ((MapStatisticsCallerCollector)dataCollector).getCount(calleName, DataCategory.TOTAL_COUNT);
    }
}
