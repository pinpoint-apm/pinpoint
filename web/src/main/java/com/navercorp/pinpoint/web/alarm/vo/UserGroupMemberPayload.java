package com.navercorp.pinpoint.web.alarm.vo;

import java.util.List;

public class UserGroupMemberPayload {
    
    private String userGroupId;
    private List<UserMember> userGroupMembers;
    
    public UserGroupMemberPayload(String userGroupId, List<UserMember> userGroupMembers) {
        this.userGroupId = userGroupId;
        this.userGroupMembers = userGroupMembers;
    }
    
    public String getUserGroupId() {
        return userGroupId;
    }
    
    public List<UserMember> getUserGroupMembers() {
        return userGroupMembers;
    }
    
    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }
    
    public void setUserGroupMembers(List<UserMember> userMembers) {
        this.userGroupMembers = userMembers;
    }
}
