package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;

import java.util.Map;
import java.util.Objects;

public class GrpcServerHeaderV1 implements ServerHeader {

    private final Header header;

    public GrpcServerHeaderV1(Header header) {
        this.header = Objects.requireNonNull(header, "header");
    }

    @Override
    public String getAgentId() {
        return header.getAgentId();
    }

    @Override
    public String getAgentName() {
        return header.getAgentName();
    }

    @Override
    public String getApplicationName() {
        return header.getApplicationName();
    }

    @Override
    public long getApplicationUid() {
        return -1;
    }

    @Override
    public String getServiceName() {
        return header.getServiceName();
    }

    @Override
    public ServiceUid getServiceUid() {
        return ServiceUid.DEFAULT_SERVICE_UID;
    }

    @Override
    public long getAgentStartTime() {
        return header.getAgentStartTime();
    }

    @Override
    public long getSocketId() {
        return header.getSocketId();
    }

    @Override
    public int getServiceType() {
        return header.getServiceType();
    }

    public boolean isGrpcBuiltInRetry() {
        return header.isGrpcBuiltInRetry();
    }

    public Object get(String key) {
        return header.get(key);
    }

    public Map<String, Object> getProperties() {
        return header.getProperties();
    }

}
