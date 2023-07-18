package com.navercorp.pinpoint.web.vo;

public class UserGroup {
    private String  number;
    private String  id;
    
    public UserGroup() {
    }
    
    public UserGroup(String number, String id) {
        this.number = number;
        this.id = id;
    }
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
}
