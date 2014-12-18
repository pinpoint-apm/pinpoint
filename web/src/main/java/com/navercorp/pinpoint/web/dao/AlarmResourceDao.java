package com.navercorp.pinpoint.web.dao;

import java.util.List;

import com.navercorp.pinpoint.web.alarm.vo.AlarmEmp;
import com.navercorp.pinpoint.web.alarm.vo.Rule;


public interface AlarmResourceDao {
    
    List<Rule> selectAppRule(String applicationName);
    
    void insertAppRule(List<Rule> rules);

    void deleteAppRule(String applicationName);
    
    List<String> selectEmpGroupPhoneNumber(String empGroup);
    
    List<String> selectEmpGroupEmail(String empGroup);
    
    List<String> selectEmpGroupName();

    List<AlarmEmp> selectEmpGroupMember(String alarmGroup);

    void insertEmpGroupMember(List<AlarmEmp> emps);

    void deleteEmpGroupMember(String groupName);
}
