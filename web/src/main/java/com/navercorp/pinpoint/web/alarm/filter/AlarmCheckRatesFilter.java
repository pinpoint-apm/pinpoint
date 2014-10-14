package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.collector.DataCollector;
import com.nhn.pinpoint.web.alarm.vo.Rule;


public abstract class AlarmCheckRatesFilter extends AlarmCheckFilter {

    protected AlarmCheckRatesFilter(Rule rule, DataCollector dataCollector) {
        super(rule, "%", dataCollector);
    }

    protected boolean check(long count, long totalCount) {
        int rates = getRates(count, totalCount);

        int threshold = getRule().getThreshold();
        
        if (rates >= threshold) {
            return true;
        } else {
            return false;
        }
    }
    
    private int getRates(long count, long totalCount) {
        int percent = 0;
        if (count == 0 || totalCount == 0) {
            return percent;
        } else {
            percent = Math.round((count * 100) / totalCount);
        }

        return percent;
    }

}
