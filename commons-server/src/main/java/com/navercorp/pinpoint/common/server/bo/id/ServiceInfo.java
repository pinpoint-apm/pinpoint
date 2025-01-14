package com.navercorp.pinpoint.common.server.bo.id;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class ServiceInfo {

    public static final UUID DEFAULT_SERVICE_UID = new UUID(0, 0);
    public static final ServiceInfo DEFAULT = new ServiceInfo(DEFAULT_SERVICE_UID, "DEFAULT", Collections.emptyMap());

    private final UUID serviceUid;
    private final String serviceName;

    private final Map<String, String> tags;

    public ServiceInfo(UUID serviceUid, String serviceName,
                       @Nullable Map<String, String> tags) {
        this.serviceUid = Objects.requireNonNull(serviceUid, "serviceUid");
        this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        this.tags = tags;
    }

    public UUID getServiceUid() {
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
