package com.navercorp.pinpoint.batch.alarm.dao;

import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmHistory;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmKey;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmRule;

import java.util.List;

public interface PinotAlarmDao {
    List<PinotAlarmKey> selectAlarmKeys(String alarmCategory);
    List<PinotAlarmRule> selectRulesByKeys(PinotAlarmKey alarmKeys);
    void insertAlarmHistory(PinotAlarmHistory history);
}
