package com.nhn.pinpoint.web.alarm;

import org.springframework.batch.item.ItemProcessor;

import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;

public class AlarmProcessor implements ItemProcessor<AlarmCheckFilter, AlarmCheckFilter> {
    
    public AlarmCheckFilter process(AlarmCheckFilter checker) {
        checker.check();
        return checker;
    }

}
