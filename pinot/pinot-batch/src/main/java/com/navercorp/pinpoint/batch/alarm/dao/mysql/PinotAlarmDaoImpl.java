package com.navercorp.pinpoint.batch.alarm.dao.mysql;

import com.navercorp.pinpoint.batch.alarm.dao.PinotAlarmDao;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmHistory;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmKey;
import com.navercorp.pinpoint.batch.alarm.vo.PinotAlarmRule;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class PinotAlarmDaoImpl implements PinotAlarmDao {
    private static final String NAMESPACE = PinotAlarmDao.class.getName() + ".";

    private final SqlSessionTemplate sqlSessionTemplate;

    public PinotAlarmDaoImpl(@Qualifier("pinotAlarmSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }
    @Override
    public List<PinotAlarmKey> selectAlarmKeys(String alarmCategory) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectAlarmKeys", alarmCategory);
    }

    @Override
    public List<PinotAlarmRule> selectRulesByKeys(PinotAlarmKey alarmKeys) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectRulesByKeys", alarmKeys);
    }

    @Override
    public void insertAlarmHistory(PinotAlarmHistory history) {
        sqlSessionTemplate.insert(NAMESPACE + "insertAlarmHistory", history);
    }
}
