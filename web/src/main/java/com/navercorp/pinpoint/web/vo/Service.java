package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;

import java.util.Objects;

public class Service {

    public static final Service DEFAULT = new Service(ServiceUid.DEFAULT_SERVICE_UID_NAME, ServiceUid.DEFAULT);
    public static final Service ERROR = new Service(ServiceUid.ERROR_SERVICE_UID_NAME, ServiceUid.ERROR);
    public static final Service UNKNOWN = new Service(ServiceUid.UNKNOWN_SERVICE_UID_NAME, ServiceUid.UNKNOWN);

    /**
     * Resolves a reserved serviceUid to its well-known Service without touching the registry.
     * Returns null for uids that require a registry lookup.
     */
    public static Service wellKnownService(int serviceUid) {
        if (serviceUid == ServiceUid.DEFAULT.getUid()) {
            return DEFAULT;
        }
        if (serviceUid == ServiceUid.ERROR.getUid()) {
            return ERROR;
        }
        if (serviceUid == ServiceUid.UNKNOWN.getUid()) {
            return UNKNOWN;
        }
        return null;
    }

    /**
     * Resolves a reserved serviceName to its well-known Service without touching the registry.
     * Returns null for names that require a registry lookup.
     */
    public static Service wellKnownService(String serviceName) {
        if (DEFAULT.getServiceName().equals(serviceName)) {
            return DEFAULT;
        }
        if (ERROR.getServiceName().equals(serviceName)) {
            return ERROR;
        }
        if (UNKNOWN.getServiceName().equals(serviceName)) {
            return UNKNOWN;
        }
        return null;
    }

    private final String serviceName;
    private final int serviceUid;

    public Service(String serviceName, int serviceUid) {
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "name");
        this.serviceUid = serviceUid;
    }

    public Service(String serviceName, ServiceUid serviceUid) {
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "name");
        Objects.requireNonNull(serviceUid, "serviceUid");
        this.serviceUid = serviceUid.getUid();
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getServiceUid() {
        return serviceUid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Service service = (Service) o;
        return serviceUid == service.serviceUid && serviceName.equals(service.serviceName);
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + serviceUid;
        return result;
    }

    @Override
    public String toString() {
        return "Service[" + serviceName + "(" + serviceUid + ")]";
    }
}
