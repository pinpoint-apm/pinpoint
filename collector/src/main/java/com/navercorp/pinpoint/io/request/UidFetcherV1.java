package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Supplier;

public class UidFetcherV1 implements UidFetcher {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final ServiceUid serviceUid = ServiceUid.DEFAULT_SERVICE_UID;

    private final UidCache cache;

    private volatile ApplicationUid applicationUid;

    private final ApplicationUidService applicationUidService;

    public UidFetcherV1(UidCache cache, ApplicationUidService applicationUidService) {
        this.cache = cache;
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "applicationUidService");
    }

    @Override
    public Supplier<ServiceUid> getServiceUid() {
        return () -> serviceUid;
    }

    @Override
    public Supplier<ApplicationUid> getApplicationId(ServiceUid serviceUid, String applicationName) {
        if (!serviceUid.equals(ServiceUid.DEFAULT_SERVICE_UID)) {
            throw new UnsupportedOperationException("Unsupported serviceUid");
        }

        final ApplicationUid copy = this.applicationUid;
        if (copy != null) {
            if (copy.equals(ApplicationUid.ERROR_APPLICATION_UID)) {
                throw new RuntimeException("Failed to fetch applicationId. applicationName:" + applicationName);
            }
            return () -> copy;
        }

        final UidCache cache = this.cache;
        if (cache != null) {
            ApplicationUid applicationUid = getApplicationUidFromCache(cache, applicationName);
            if (applicationUid != null) {
                return () -> applicationUid;
            }
        }
        // blocking
        prefetch(applicationName);
        return () -> this.applicationUid;
    }

    private ApplicationUid getApplicationUidFromCache(UidCache cache, String applicationName) {
        return cache.getApplicationUid(serviceUid, applicationName);
    }

    public void prefetch(String applicationName) {
        synchronized (this) {
            if (this.applicationUid != null) {
                // already fetched
                return;
            }
            else {
                // fetch serviceUid & applicationUid from server
                ApplicationUid applicationId = fetchApplicationUid(applicationName);
                this.applicationUid = applicationId;
                this.cache.put(ServiceUid.DEFAULT_SERVICE_UID, applicationName, applicationId);
            }
        }
    }

    private ApplicationUid fetchApplicationUid(String applicationName) {
        try {
            return this.applicationUidService.getApplicationId(ServiceUid.DEFAULT_SERVICE_UID, applicationName);
        } catch (Throwable e) {
            logger.info("Failed to fetch applicationId. applicationName:{}", applicationName, e);
            return ApplicationUid.ERROR_APPLICATION_UID;
        }
    }
}
