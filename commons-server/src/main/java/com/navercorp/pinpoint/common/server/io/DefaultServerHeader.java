package com.navercorp.pinpoint.common.server.io;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.Supplier;

public class DefaultServerHeader implements ServerHeader {

    private final String agentId;
    private final String agentName;
    private final String applicationName;
    private final String serviceName;

    private final long agentStartTime;
    private final int serviceType;

    private final Supplier<ServiceUid> uidSupplier;
    private final boolean grpcBuiltInRetry;

    public DefaultServerHeader(String agentId, String agentName, String applicationName, String serviceName,
                               Supplier<ServiceUid> uidSupplier,
                               long agentStartTime, int serviceType, boolean grpcBuiltInRetry) {
        this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        this.agentName = StringPrecondition.requireHasLength(agentName, "agentName");
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.serviceName = StringPrecondition.requireHasLength(serviceName, "serviceName");

        this.uidSupplier = Objects.requireNonNull(uidSupplier, "uidSupplier");

        this.agentStartTime = agentStartTime;
        this.serviceType = serviceType;
        this.grpcBuiltInRetry = grpcBuiltInRetry;
    }

    @NonNull
    @Override
    public String getAgentId() {
        return agentId;
    }


    @NonNull
    @Override
    public String getAgentName() {
        return agentName;
    }

    @NonNull
    @Override
    public  String getApplicationName() {
        return applicationName;
    }

    @NonNull
    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public Supplier<ServiceUid> getServiceUid() {
        return uidSupplier;
    }

    @Override
    public long getAgentStartTime() {
        return agentStartTime;
    }

    @Override
    public int getServiceType() {
        return serviceType;
    }

    @Override
    public boolean isGrpcBuiltInRetry() {
        return grpcBuiltInRetry;
    }


    @Override
    public String toString() {
        return "DefaultServerHeader{" +
                "agentId='" + agentId + '\'' +
                ", agentName='" + agentName + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", agentStartTime=" + agentStartTime +
                ", serviceType=" + serviceType +
                ", uidSupplier=" + uidSupplier +
                ", grpcBuiltInRetry=" + grpcBuiltInRetry +
                '}';
    }
}
