package com.navercorp.pinpoint.service.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ServiceEntry {
    private int uid;
    private String name;

    public ServiceEntry() {
    }

    @JsonIgnore
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
