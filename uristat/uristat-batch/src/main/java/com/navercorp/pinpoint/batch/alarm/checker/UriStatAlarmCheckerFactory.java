package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.batch.alarm.collector.PinotDataCollector;
import com.navercorp.pinpoint.batch.alarm.condition.AlarmCondition;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmRule;

import java.util.List;

public interface UriStatAlarmCheckerFactory {
    PinotAlarmChecker<?> createChecker(List<PinotAlarmRule> rule, PinotDataCollector dataCollector, AlarmCondition alarmCondition);

}
