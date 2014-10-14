package com.nhn.pinpoint.web.alarm;

import org.springframework.batch.item.ItemProcessor;

import com.nhn.pinpoint.web.alarm.checker.AlarmChecker;

public class AlarmProcessor implements ItemProcessor<AlarmChecker, AlarmChecker> {
    
    public AlarmChecker process(AlarmChecker checker) {
        checker.check();
        return checker;
    }

}
