package com.navercorp.pinpoint.web.vo;

public class UserGroupMember {
    
    private String userGroupId;
    private String memberId;
    
    public UserGroupMember() {
    }
    
    public UserGroupMember(String userGroupId, String memberId) {
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
