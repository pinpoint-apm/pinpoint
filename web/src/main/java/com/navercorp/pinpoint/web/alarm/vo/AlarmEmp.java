package com.nhn.pinpoint.web.alarm.vo;

import org.apache.ibatis.type.Alias;

@Alias(value = "alarmEmp")
public class AlarmEmp {
    
    private String id = "0";
    private String groupName;
    private String empName;
    private String sms;
    private String email;
    
    public AlarmEmp() {
    }
    
    public AlarmEmp(String groupName, String empName, String sms, String email) {
        this.groupName = groupName;
        this.empName = empName;
        this.sms = sms;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    

}
