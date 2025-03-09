package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.grpc.Header;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;


public class GrpcServerHeaderV1 implements ServerHeader {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Header header;
    private final UidFetcher uidFetcher;

    private volatile ApplicationUid applicationUid;

    public GrpcServerHeaderV1(Header header) {
        this(header, null);
    }

    public GrpcServerHeaderV1(Header header, UidFetcher uidFetcher) {
        this.header = Objects.requireNonNull(header, "header");
        this.uidFetcher = uidFetcher;
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
        final ApplicationUid copy = this.applicationUid;
        if (copy != null) {
            if (copy.equals(ApplicationUid.ERROR_APPLICATION_UID)) {
                throw new RuntimeException("Failed to fetch applicationId. applicationName:" + getApplicationName());
            }
            return copy;
        }

        final UidFetcher fetcher = this.uidFetcher;
        if (fetcher != null) {
            String applicationName = getApplicationName();
            Supplier<ApplicationUid> supplier = fetcher.getApplicationId(ServiceUid.DEFAULT_SERVICE_UID, applicationName);
            ApplicationUid applicationUid = supplier.get();
            if (applicationUid != null) {
                return applicationUid;
            }
        }
        return this.applicationUid;
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
