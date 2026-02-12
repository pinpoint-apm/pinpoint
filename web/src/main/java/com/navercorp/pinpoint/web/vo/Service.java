package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;

import java.util.Objects;

public class Service {

    public static final Service DEFAULT = new Service(ServiceUid.DEFAULT_SERVICE_UID_NAME, ServiceUid.DEFAULT_SERVICE_UID_CODE);

    private final String serviceName;
    private final int uid;

    public Service(String serviceName, int uid) {
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "name");
        this.uid = uid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getUid() {
        return uid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;
        return uid == service.uid && Objects.equals(serviceName, service.serviceName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(serviceName);
        result = 31 * result + uid;
        return result;
    }

    @Override
    public String toString() {
        return "Service[" + serviceName + "(" + uid + ")]";
    }
}
