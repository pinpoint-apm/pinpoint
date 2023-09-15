package com.navercorp.pinpoint.pinot.alarm.service;

import com.navercorp.pinpoint.pinot.alarm.dao.PinotAlarmDao;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmHistory;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmKey;
import com.navercorp.pinpoint.pinot.alarm.vo.PinotAlarmRule;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PinotAlarmService {
    private final PinotAlarmDao alarmDao;

    public PinotAlarmService(@NonNull PinotAlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

    public List<PinotAlarmKey> selectAlarmKeys(String alarmCategory) {
        return alarmDao.selectAlarmKeys(alarmCategory);
    }

    public List<PinotAlarmRule> selectRulesByKeys(PinotAlarmKey alarmKeys) {
        return alarmDao.selectRulesByKeys(alarmKeys);
    }

    public void insertAlarmHistory(PinotAlarmHistory history) {
        alarmDao.insertAlarmHistory(history);
    }
}
