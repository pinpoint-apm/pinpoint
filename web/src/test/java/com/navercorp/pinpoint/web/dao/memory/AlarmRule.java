package com.navercorp.pinpoint.web.dao.memory;

import com.navercorp.pinpoint.web.alarm.vo.Rule;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AlarmRule {
    private final Map<String, Rule> alarmRule = new ConcurrentHashMap<>();
    private final AtomicInteger ruleIdGenerator  = new AtomicInteger();

    public Map<String, Rule> getAlarmRule() {
        return alarmRule;
    }

    public AtomicInteger getRuleIdGenerator() {
        return ruleIdGenerator;
    }
}
