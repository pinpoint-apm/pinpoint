package com.navercorp.pinpoint.web.vo;

public class UserGroupMember {
    
    private String userGroupId;
    private String memberId;
    private String name;
    private String department;
    
    public UserGroupMember() {
    }

    public UserGroupMember(String userGroupId, String memberId) {
        this.userGroupId = userGroupId;
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
}
