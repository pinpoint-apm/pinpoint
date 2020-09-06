package com.navercorp.pinpoint.web.alarm.vo;

import com.navercorp.pinpoint.web.vo.UserGroupMember;

public class UserMember {
    private String number;
    private String memberId;
    private String name;
    private String department;
    
    public UserMember(UserGroupMember userGroupMember) {
        this.name = userGroupMember.getName();
        this.memberId = userGroupMember.getMemberId();
        this.name = userGroupMember.getName();
        this.department = userGroupMember.getDepartment();
    }
  
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getMemberId() {
        return memberId;
    }
    
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "UserMember{" +
            "number='" + number + '\'' +
            ", memberId='" + memberId + '\'' +
            ", name='" + name + '\'' +
            ", department='" + department + '\'' +
            '}';
    }
}
