package com.nhn.pinpoint.web.dao.mysql;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.alarm.vo.AlarmEmp;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.dao.AlarmResourceDao;

@Repository
public class MySqlAlarmResourceDao implements AlarmResourceDao {

    private static final String NAMESPACE = AlarmResourceDao.class.getPackage().getName() + "." + AlarmResourceDao.class.getSimpleName() + ".";

    @Autowired
    @Qualifier("sqlSessionTemplate")
    private SqlSessionTemplate sqlSessionTemplate;

    public SqlSessionTemplate getSqlSessionTemplate() {
        return sqlSessionTemplate;
    }

    public List<Rule> selectAppRule(String applicationName) {
        return getSqlSessionTemplate().selectList(NAMESPACE + "selectRules", applicationName);
    }
    
    public void insertAppRule(List<Rule> rules) {
        getSqlSessionTemplate().selectList(NAMESPACE + "insertAppRule", rules);
    }

    public void deleteAppRule(String applicationName) {
        getSqlSessionTemplate().selectList(NAMESPACE + "deleteAppRule", applicationName);
    }
    
    public List<String> selectEmpGroupPhoneNumber(String empGroup) {
        return getSqlSessionTemplate().selectList(NAMESPACE + "selectEmpGroupPhoneNumber", empGroup);
    }
    
    public List<String> selectEmpGroupEmail(String empGroup) {
        return getSqlSessionTemplate().selectList(NAMESPACE + "selectEmpGroupEmail", empGroup);
    }
    
    public List<String> selectEmpGroupName() {
        return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmGroupList");
    }

    public List<AlarmEmp> selectEmpGroupMember(String alarmGroup) {
        return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmGroupMember", alarmGroup);
    }

    public void insertEmpGroupMember(List<AlarmEmp> emps) {
        getSqlSessionTemplate().insert(NAMESPACE + "insertAlarmGroupMember", emps);
    }

    public void deleteEmpGroupMember(String groupName) {
        getSqlSessionTemplate().insert(NAMESPACE + "deleteAlarmGroupMember", groupName);
    }
}
