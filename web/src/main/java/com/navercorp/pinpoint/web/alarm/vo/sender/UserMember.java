package com.navercorp.pinpoint.web.alarm.vo.sender;

import com.navercorp.pinpoint.web.vo.UserGroupMember;

public class UserMember {

    private String memberId;
    private String name;
    private String email;
    private String department;
    private String phoneNumber;
    private int phoneCountryCode;
    
    public UserMember() {
    }
    
    public static UserMember from(UserGroupMember userGroupMember) {
        UserMember userMember = new UserMember();
        userMember.memberId = userGroupMember.getMemberId();
        userMember.name = userGroupMember.getName();
        userMember.email = userGroupMember.getEmail();
        userMember.department = userGroupMember.getDepartment();
        userMember.phoneNumber = userGroupMember.getPhoneNumber();
        userMember.phoneCountryCode = userGroupMember.getPhoneCountryCode();
        return userMember;
    }
    
    public String getMemberId() {
        return memberId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public int getPhoneCountryCode() {
        return phoneCountryCode;
    }
}
