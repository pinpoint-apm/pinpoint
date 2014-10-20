package com.nhn.pinpoint.web.alarm.checker;

import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerCollector;
import com.nhn.pinpoint.web.alarm.collector.MapStatisticsCallerCollector.DataCategory;
import com.nhn.pinpoint.web.alarm.vo.Rule;

public class SlowCountToCalleChecker extends AlarmChecker {
    
    public SlowCountToCalleChecker(MapStatisticsCallerCollector dataCollector, Rule rule) {
        super(rule, "", dataCollector);
    }
    
    @Override
    protected long getDetectedValue() {
        String calleName = rule.getNotes();
        return ((MapStatisticsCallerCollector)dataCollector).getCount(calleName, DataCategory.SLOW_COUNT);
    }
    
    @Override
    public String getEmailMessage() {
        return String.format("%s value is %s%s during the past 5 mins.(Threshold : %s%s) %s For From $s To $s.<br>", rule.getCheckerName(), getDetectedValue(), unit, rule.getThreshold(), unit, rule.getCheckerName(), rule.getApplicationId(), rule.getNotes());
    };

}
