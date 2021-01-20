package com.navercorp.pinpoint.batch.alarm.vo.sender.payload;

import org.apache.ibatis.type.Alias;

import java.util.List;

@Alias(value = "UserGroupPayload")
public class UserGroup {
    
    private String userGroupId;
    private List<UserMember> userGroupMembers;
    
    public UserGroup(String userGroupId, List<UserMember> userGroupMembers) {
        this.userGroupId = userGroupId;
        this.userGroupMembers = userGroupMembers;
    }
    
    public String getUserGroupId() {
        return userGroupId;
    }
    
    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }
    
    public List<UserMember> getUserGroupMembers() {
        return userGroupMembers;
    }
    
    public void setUserGroupMembers(List<UserMember> userGroupMembers) {
        this.userGroupMembers = userGroupMembers;
    }
}
