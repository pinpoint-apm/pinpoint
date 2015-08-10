package com.navercorp.pinpoint.web.vo;

public class UserGroupMember {
    
    private String number;
    private String userGroupId;
    private String memberId;
    
    public UserGroupMember() {
    }

    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public UserGroupMember(String number, String userGroupId, String memberId) {
        this.userGroupId = userGroupId;
        this.memberId  = memberId;
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

}
