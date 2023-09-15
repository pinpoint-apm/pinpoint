package com.navercorp.pinpoint.batch.alarm.checker;

import com.navercorp.pinpoint.pinot.alarm.collector.PinotDataCollector;
import com.navercorp.pinpoint.pinot.alarm.condition.AlarmCondition;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmRule;
import com.navercorp.pinpoint.pinot.alarm.checker.PinotAlarmChecker;

import java.util.List;

public interface UriStatAlarmCheckerFactory {
    PinotAlarmChecker<?> createChecker(List<PinotAlarmRule> rule, PinotDataCollector dataCollector, AlarmCondition alarmCondition);

}
