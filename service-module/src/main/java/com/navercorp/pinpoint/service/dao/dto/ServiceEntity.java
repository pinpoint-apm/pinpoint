package com.navercorp.pinpoint.service.dao.dto;

import java.util.Objects;

public class ServiceEntity {

    private int uid;
    private String name;

    public ServiceEntity() {
    }

    public ServiceEntity(int uid, String name) {
        this.uid = uid;
        this.name = Objects.requireNonNull(name, "name");
    }

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
