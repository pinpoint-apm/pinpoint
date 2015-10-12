package com.navercorp.pinpoint.web.vo;

public class User {
    private String number;
    private String userId;
    private String name;
    private String department;
    private String phoneNumber;
    private String email;
    
    public User() {
    }
    
    public User(String userId, String name, String department, String phoneNumber, String email) {
        this.userId = userId;
        this.name = name;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void removeHyphenForPhoneNumber() {
        if (phoneNumber != null && phoneNumber.contains("-")) {
            phoneNumber = phoneNumber.replace("-", "");
        }
    }
}
