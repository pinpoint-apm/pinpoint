package com.navercorp.pinpoint.web.vo;

public class UserGroupMember {
    
    private String number;
    private String userGroupId;
    private String memberId;
    private String name;
    private String department;
    
    public UserGroupMember() {
    }

    public UserGroupMember(String userGroupId, String memberId) {
        this.userGroupId = userGroupId;
        this.memberId = memberId;
    }

    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getUserGroupId() {
        return userGroupId;
    }
    
    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
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
        return "UserGroupMember{" +
            "number='" + number + '\'' +
            ", userGroupId='" + userGroupId + '\'' +
            ", memberId='" + memberId + '\'' +
            ", name='" + name + '\'' +
            ", department='" + department + '\'' +
            '}';
    }
}
