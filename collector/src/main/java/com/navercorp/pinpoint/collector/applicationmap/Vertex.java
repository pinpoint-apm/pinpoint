package com.navercorp.pinpoint.collector.applicationmap;

import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

public record Vertex(String applicationName, ServiceType serviceType) {

    public Vertex(String applicationName, ServiceType serviceType) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }


    public static Vertex of(String applicationName, ServiceType serviceType) {
        return new Vertex(applicationName, serviceType);
    }

    @Override
    public String toString() {
        return applicationName + "/" + serviceType.getName() + ':' + serviceType.getCode();
    }
}
