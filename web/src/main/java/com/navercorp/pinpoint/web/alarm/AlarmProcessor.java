package com.navercorp.pinpoint.web.alarm;

import org.springframework.batch.item.ItemProcessor;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;

public class AlarmProcessor implements ItemProcessor<AlarmChecker, AlarmChecker> {
    
    public AlarmChecker process(AlarmChecker checker) {
        checker.check();
        return checker;
    }

}
