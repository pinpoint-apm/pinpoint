package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.io.request.supplier.UidSuppliers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class UidFetcherV1 implements UidFetcher {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final ServiceUid DEFAULT_SERVICE_UID = ServiceUid.DEFAULT;

    private final UidCache cache;

    private volatile ApplicationUid applicationUid;

    private final ApplicationUidService applicationUidCacheService;

    private final ReentrantLock lock = new ReentrantLock();

    public UidFetcherV1(ApplicationUidService applicationUidCacheService, UidCache cache) {
        this.applicationUidCacheService = Objects.requireNonNull(applicationUidCacheService, "applicationUidService");
        this.cache = cache;
    }

    @Override
    public Supplier<ServiceUid> getServiceUid() {
        return () -> DEFAULT_SERVICE_UID;
    }

    @Override
    public Supplier<ApplicationUid> getApplicationId(ServiceUid serviceUid, String applicationName) {
        if (!serviceUid.equals(DEFAULT_SERVICE_UID)) {
            throw new UnsupportedOperationException("Unsupported serviceUid");
        }

        final ApplicationUid copy = this.applicationUid;
        if (copy != null) {
            return UidSuppliers.of(applicationName, copy);
        }

        final UidCache cache = this.cache;
        if (cache != null) {
            final ApplicationUid cachedUid = getApplicationUidFromCache(cache, applicationName);
            if (cachedUid != null) {
                return UidSuppliers.of(applicationName, cachedUid);
            }
        }
        // blocking
        return () -> prefetch(applicationName);
    }


    private ApplicationUid getApplicationUidFromCache(UidCache cache, String applicationName) {
        return cache.getApplicationUid(DEFAULT_SERVICE_UID, applicationName);
    }

    public ApplicationUid prefetch(String applicationName) {
        lock.lock();
        try {
            ApplicationUid copy = this.applicationUid;
            if (copy != null) {
                // already fetched
                return copy;
            } else {
                // fetch serviceUid & applicationUid from server
                ApplicationUid applicationId = fetchApplicationUid(applicationName);
                this.cache.put(DEFAULT_SERVICE_UID, applicationName, applicationId);
                this.applicationUid = applicationId;
                return applicationId;
            }
        } finally {
            lock.unlock();
        }
    }

    private ApplicationUid fetchApplicationUid(String applicationName) {
        try {
            return this.applicationUidCacheService.getOrCreateApplicationUid(DEFAULT_SERVICE_UID, applicationName);
        } catch (Throwable e) {
            logger.info("Failed to fetch applicationId. applicationName:{}", applicationName, e);
            return ApplicationUid.ERROR_APPLICATION_UID;
        }
    }
}
