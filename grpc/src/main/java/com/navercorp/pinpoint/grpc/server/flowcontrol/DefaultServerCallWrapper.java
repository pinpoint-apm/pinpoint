package com.navercorp.pinpoint.grpc.server.flowcontrol;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;

import java.util.Objects;

public class DefaultServerCallWrapper<ReqT, RespT> implements ServerCallWrapper {
    private final ServerCall<ReqT, RespT> serverCall;
    private final String agentId;
    private final String applicationName;
    private final String serviceId;

    public DefaultServerCallWrapper(ServerCall<ReqT, RespT> serverCall, String serviceId, String applicationName, String agentId) {
        this.serverCall = Objects.requireNonNull(serverCall, "serverCall");
        this.serviceId = serviceId;
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.agentId = Objects.requireNonNull(agentId, "agentId");
    }

    @Override
    public void request(int numMessages) {
        this.serverCall.request(numMessages);
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void cancel(Status status, Metadata trailers) {
        this.serverCall.close(status, new Metadata());
    }

    @Override
    public String toString() {
        return "DefaultServerCallWrapper{" +
                "serverCall=" + serverCall +
                ", agentId='" + agentId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }
}
