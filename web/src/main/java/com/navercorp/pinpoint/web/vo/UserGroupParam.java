package com.navercorp.pinpoint.web.vo;

public class UserGroupParam extends UserGroup {
    private String userId;
    
    public UserGroupParam() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}