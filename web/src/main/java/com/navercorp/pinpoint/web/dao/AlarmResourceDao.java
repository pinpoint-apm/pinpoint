package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.alarm.vo.AlarmContactGroupResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmContactResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleGroupResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;
import com.nhn.pinpoint.web.alarm.vo.Rule;


public interface AlarmResourceDao {

    public List<Rule> selectAppRule(String applicationName);

    public List<String> selectEmpGroupPhoneNumber(String empGroup);
    
    public List<String> selectEmpGroupEmail(String empGroup);
    
    int selectAlarmCount();

    List<AlarmResource> selectAlarmList();

    List<AlarmRuleResource> selectAlarmRuleList();
    
    List<AlarmRuleGroupResource> selectAlarmRuleGroupList();
       
    void insertAlarmContact(AlarmContactResource resource);
    List<AlarmContactResource> selectAlarmContactList();
    void updateAlarmCountact(AlarmContactResource resource);
    void deleteAlarmCountact(AlarmContactResource resource);
    
    void insertAlarmContactGroup(AlarmContactGroupResource resource);
    List<AlarmContactGroupResource> selectAlarmContactGroupList();
    void updateAlarmContactGroup(AlarmContactGroupResource resource);
    void deleteAlarmCountactGroup(AlarmContactGroupResource resource);

    
}
