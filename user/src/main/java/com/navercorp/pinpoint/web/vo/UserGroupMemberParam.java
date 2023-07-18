package com.navercorp.pinpoint.web.vo;

public class UserGroupMemberParam extends UserGroupMember {
    private String userId;

    public UserGroupMemberParam() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}