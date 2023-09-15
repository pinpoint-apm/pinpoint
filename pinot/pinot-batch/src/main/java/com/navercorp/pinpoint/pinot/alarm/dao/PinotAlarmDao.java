package com.navercorp.pinpoint.pinot.alarm.dao;

import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmHistory;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmKey;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmRule;

import java.util.List;

public interface PinotAlarmDao {
    List<PinotAlarmKey> selectAlarmKeys(String alarmCategory);
    List<PinotAlarmRule> selectRulesByKeys(PinotAlarmKey alarmKeys);
    void insertAlarmHistory(PinotAlarmHistory history);
}
