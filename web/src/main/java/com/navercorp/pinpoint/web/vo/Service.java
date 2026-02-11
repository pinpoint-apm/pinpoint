package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.Objects;

public class Service {

    public static final Service DEFAULT = new Service(ServiceUid.DEFAULT_SERVICE_UID_NAME, ServiceUid.DEFAULT_SERVICE_UID_CODE);

    private final String name;
    private final int uid;

    public Service(String name, int uid) {
        this.name = name;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public int getUid() {
        return uid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;
        return uid == service.uid && Objects.equals(name, service.name);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + uid;
        return result;
    }

    @Override
    public String toString() {
        return "Service[" + name + ":" + uid + "]";
    }
}
