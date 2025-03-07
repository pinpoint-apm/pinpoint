package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;

import java.util.Map;
import java.util.Objects;

public class GrpcServerHeaderV1 implements ServerHeader {

    private final Header header;
    private final UidCache cache;

    private volatile ApplicationUid applicationUid;

    public GrpcServerHeaderV1(Header header) {
        this(header, null);
    }

    public GrpcServerHeaderV1(Header header, UidCache cache) {
        this.header = Objects.requireNonNull(header, "header");
        this.cache = cache;
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
    public ApplicationUid getApplicationUid() {
        if (this.applicationUid != null) {
            return applicationUid;
        }

        final UidCache cache = this.cache;
        if (cache != null) {
            ApplicationUid applicationUid = getApplicationUidFromCache(cache);
            if (applicationUid != null) {
                return applicationUid;
            }
        }
        prefetch();
        return this.applicationUid;
    }

    private ApplicationUid getApplicationUidFromCache(UidCache cache) {
        ServiceUid serviceUid = getServiceUid();
        String applicationName = getApplicationName();
        return cache.getApplicationUid(serviceUid, applicationName);
    }

    public void prefetch() {
        synchronized (this) {
            if (this.applicationUid != null) {
                // already fetched
                return;
            }
            else {
                // fetch serviceUid & applicationUid from server
                ApplicationUid testUid = ApplicationUid.of(100);
                this.cache.put(ServiceUid.DEFAULT_SERVICE_UID, getApplicationName(), testUid);
                this.applicationUid = testUid;
            }
        }
    }

    @Override
    public String getServiceName() {
        return header.getServiceName();
    }

    @Override
    public ServiceUid getServiceUid() {
        final UidCache cache = this.cache;
        if (cache != null) {
            ServiceUid serviceUid = cache.getServiceUid(getServiceName());
            if (serviceUid != null) {
                return serviceUid;
            }
        }
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
