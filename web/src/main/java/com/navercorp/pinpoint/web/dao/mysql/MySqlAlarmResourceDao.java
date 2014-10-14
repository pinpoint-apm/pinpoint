package com.nhn.pinpoint.web.dao.mysql;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.alarm.vo.AlarmContactGroupResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmContactResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmEmp;
import com.nhn.pinpoint.web.alarm.vo.AlarmResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleGroupResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;
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
    
    @Override
    public int selectAlarmCount() {
        return 1;
    }
    
    @Override
    public List<AlarmResource> selectAlarmList() {
        return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmList");
    }

    @Override
    public List<AlarmRuleResource> selectAlarmRuleList() {
        return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmRuleList", 1);
    }
    
    @Override
    public List<AlarmRuleGroupResource> selectAlarmRuleGroupList() {
        return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmRuleGroupList", 1);
    }

    @Override
    public List<AlarmContactResource> selectAlarmContactList() {
        return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmContactList", 1);
    }
    
    @Override
    public List<AlarmContactGroupResource> selectAlarmContactGroupList() {
        return getSqlSessionTemplate().selectList(NAMESPACE + "selectAlarmContactGroupList", 1);
    }

    
    @Override
    public void insertAlarmContact(AlarmContactResource resource) {
        getSqlSessionTemplate().insert(NAMESPACE + "insertAlarmContact", resource);
    }
    
    @Override
    public void updateAlarmCountact(AlarmContactResource resource) {
        getSqlSessionTemplate().update(NAMESPACE + "updateAlarmContact", resource);
    }
    
    @Override
    public void deleteAlarmCountact(AlarmContactResource resource) {
        getSqlSessionTemplate().update(NAMESPACE + "deleteAlarmContact", resource.getId());
    }

    @Override
    public void insertAlarmContactGroup(AlarmContactGroupResource resource) {
        getSqlSessionTemplate().insert(NAMESPACE + "insertAlarmContactGroup", resource);
    }
    
    @Override
    public void updateAlarmContactGroup(AlarmContactGroupResource resource) {
        getSqlSessionTemplate().insert(NAMESPACE + "updateAlarmContactGroup", resource);
    }
    
    @Override
    public void deleteAlarmCountactGroup(AlarmContactGroupResource resource) {
        getSqlSessionTemplate().update(NAMESPACE + "deleteAlarmContactGroup", resource.getId());
    }
}
