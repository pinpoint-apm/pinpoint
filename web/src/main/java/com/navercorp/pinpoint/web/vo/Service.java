package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;

import java.util.Objects;

public class Service {

    public static final Service DEFAULT = new Service(ServiceUid.DEFAULT_SERVICE_UID_NAME, ServiceUid.DEFAULT);
    public static final Service TEST_SERVICE = new Service(ServiceUid.TEST_SERVICE_UID_NAME, ServiceUid.TEST_SERVICE);

    private final String serviceName;
    private final ServiceUid serviceUid;

    public Service(String serviceName, int serviceUid) {
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "name");
        this.serviceUid = ServiceUid.of(serviceUid);
    }

    public Service(String serviceName, ServiceUid serviceUid) {
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "name");
        this.serviceUid = serviceUid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ServiceUid getServiceUid() {
        return serviceUid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;
        return serviceUid.equals(service.serviceUid) && serviceName.equals(service.serviceName);
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + serviceUid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Service[" + serviceName + "(" + serviceUid + ")]";
    }
}
