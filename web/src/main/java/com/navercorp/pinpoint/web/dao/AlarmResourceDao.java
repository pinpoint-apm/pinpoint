package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.alarm.vo.AlarmEmp;
import com.nhn.pinpoint.web.alarm.vo.Rule;


public interface AlarmResourceDao {
    
    public List<Rule> selectAppRule(String applicationName);
    
    public void insertAppRule(List<Rule> rules);

    public void deleteAppRule(String applicationName);
    
    public List<String> selectEmpGroupPhoneNumber(String empGroup);
    
    public List<String> selectEmpGroupEmail(String empGroup);
    
    public List<String> selectEmpGroupName();

    public List<AlarmEmp> selectEmpGroupMember(String alarmGroup);

    public void insertEmpGroupMember(List<AlarmEmp> emps);

    public void deleteEmpGroupMember(String groupName);
}
