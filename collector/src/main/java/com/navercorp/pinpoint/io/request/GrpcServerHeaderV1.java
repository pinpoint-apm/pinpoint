package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.io.request.supplier.UidSuppliers;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;


public class GrpcServerHeaderV1 implements ServerHeader {

    private final Header header;
    private final UidFetcher uidFetcher;

    public GrpcServerHeaderV1(Header header) {
        this(header, UidFetchers.empty());
    }

    public GrpcServerHeaderV1(Header header, UidFetcher uidFetcher) {
        this.header = Objects.requireNonNull(header, "header");
        this.uidFetcher = Objects.requireNonNull(uidFetcher, "uidFetcher");
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
    public Supplier<ApplicationUid> getApplicationUid() {
        String applicationName = getApplicationName();
        CompletableFuture<ApplicationUid> future = this.uidFetcher.getApplicationUid(ServiceUid.DEFAULT, applicationName, getServiceType());
        return UidSuppliers.of(applicationName, future);
    }

    @Override
    public String getServiceName() {
        return header.getServiceName();
    }

    @Override
    public Supplier<ServiceUid> getServiceUid() {
        return () -> ServiceUid.DEFAULT;
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

    @Override
    public String toString() {
        return "GrpcServerHeaderV1{" +
                "header=" + header +
                '}';
    }
}
