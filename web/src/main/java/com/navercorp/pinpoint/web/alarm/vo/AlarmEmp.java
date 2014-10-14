package com.nhn.pinpoint.web.alarm.vo;

import org.apache.ibatis.type.Alias;

@Alias(value = "alarmEmp")
public class AlarmEmp {
    
    private String id = "0";
    private String groupName;
    private String empId;
    
    public AlarmEmp() {
    }
    
    public AlarmEmp(String groupName, String empId) {
        this.groupName = groupName;
        this.empId = empId;
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

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    

}
