package com.nhn.pinpoint.web.alarm.filter;

import com.nhn.pinpoint.web.alarm.vo.Rule;

public abstract class AlarmCheckCountFilter extends AlarmCheckFilter {

    protected AlarmCheckCountFilter(Rule rule, String unit) {
        super(rule, unit);
    }
    
    protected boolean decideResult(long count) {
        if (count >= rule.getThreshold()) {
            return true;
        } else {
            return false;
        }
    }

}
