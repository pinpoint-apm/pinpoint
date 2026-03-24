package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServiceNodeName {
    public static final String SERVICE_DELIMITER = NodeName.NODE_DELIMITER;
    public static final String NODE_DELIMITER = NodeName.NODE_DELIMITER;

    private final String serviceName;
    private final String applicationName;
    private final ServiceType serviceType;

    public static ServiceNodeName of(Application application) {
        Objects.requireNonNull(application, "application");
        return new ServiceNodeName(application.getService().getServiceName(), application.getApplicationName(), application.getServiceType());
    }

    public ServiceNodeName(String serviceName, String applicationName, ServiceType serviceType) {
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    public String getName() {
        return toServiceNodeName(serviceName, applicationName, serviceType);
    }

    public static String toServiceNodeName(String serviceName, String applicationName, ServiceType serviceType) {
        return newServiceNodeKey(serviceName, applicationName, serviceType.getDesc());
    }

    public static String toServiceNodeKey(String serviceName, String applicationName, ServiceType serviceType) {
        return newServiceNodeKey(serviceName, applicationName, serviceType.getName());
    }

    static String newServiceNodeKey(String serviceName, String applicationName, String serviceType) {
        return serviceName + SERVICE_DELIMITER + ApplicationNameEscaper.escape(applicationName) + NODE_DELIMITER + serviceType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ServiceNodeName that = (ServiceNodeName) o;
        return serviceName.equals(that.serviceName) && applicationName.equals(that.applicationName) && serviceType.equals(that.serviceType);
    }

    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        result = 31 * result + applicationName.hashCode();
        result = 31 * result + serviceType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }
}
