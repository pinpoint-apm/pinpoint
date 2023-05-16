package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.alarm.collector.DataCollector;
import com.navercorp.pinpoint.web.alarm.vo.Rule;

public interface AlarmCheckerFactory {
    AlarmChecker<?> createChecker(DataCollector dataCollector, Rule rule);

}
