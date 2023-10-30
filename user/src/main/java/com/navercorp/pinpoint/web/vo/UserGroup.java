package com.navercorp.pinpoint.web.vo;

import java.io.Serializable;

public class UserGroup implements Serializable {
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
