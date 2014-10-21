package com.nhn.pinpoint.web.alarm.checker;

import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector;
import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerDataCollector.DataCategory;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class ErrorRateToCalleChecker extends AlarmChecker {

    public ErrorRateToCalleChecker(DataCollector dataCollector, Rule rule) {
        super(rule, "%", dataCollector);
    }

    @Override
    protected long getDetectedValue() {
        String calleName = rule.getNotes();
        return ((MapStatisticsCallerDataCollector)dataCollector).getCountRate(calleName, DataCategory.ERROR_RATE);
    }
    
    @Override
    public String getEmailMessage() {
        return String.format("%s value is %s%s during the past 5 mins.(Threshold : %s%s) %s For From $s To $s.<br>", rule.getCheckerName(), getDetectedValue(), unit, rule.getThreshold(), unit, rule.getCheckerName(), rule.getApplicationId(), rule.getNotes());
    }
}
