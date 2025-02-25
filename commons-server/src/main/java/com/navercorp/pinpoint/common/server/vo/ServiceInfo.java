package com.navercorp.pinpoint.common.server.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.annotation.Nullable;

import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class ServiceInfo {

    private final ServiceUid serviceUid;
    private final String serviceName;

    private final Map<String, String> tags;

    public ServiceInfo(ServiceUid serviceUid, String serviceName,
                       @Nullable Map<String, String> tags) {
        this.serviceUid = Objects.requireNonNull(serviceUid, "serviceUid");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.tags = tags;
    }

    @JsonUnwrapped
    public ServiceUid getServiceUid() {
        return serviceUid;
    }

    public String getServiceName() {
        return serviceName;
    }

    @JsonInclude(NON_NULL)
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceInfo that = (ServiceInfo) o;

        return serviceUid.equals(that.serviceUid);
    }

    @Override
    public int hashCode() {
        return serviceUid.hashCode();
    }
}
