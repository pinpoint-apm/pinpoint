package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class UidFetcherV1 implements UidFetcher {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private static final ServiceUid DEFAULT_SERVICE_UID = ServiceUid.DEFAULT;

    private final ApplicationUidService applicationUidCacheService;
    private final UidCache cache;

    public UidFetcherV1(ApplicationUidService applicationUidCacheService, UidCache cache) {
        this.applicationUidCacheService = Objects.requireNonNull(applicationUidCacheService, "applicationUidService");
        this.cache = cache;
    }

    @Override
    public CompletableFuture<ServiceUid> getServiceUid() {
        return CompletableFuture.completedFuture(ServiceUid.DEFAULT);
    }

    @Override
    public CompletableFuture<ApplicationUid> getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        if (!serviceUid.equals(DEFAULT_SERVICE_UID)) {
            throw new UnsupportedOperationException("Unsupported serviceUid");
        }

        if (cache != null) {
            final ApplicationUid cachedUid = cache.getApplicationUid(serviceUid, applicationName, serviceTypeCode);
            if (cachedUid != null) {
                return CompletableFuture.completedFuture(cachedUid);
            }
        }

        return prefetch(serviceUid, applicationName, serviceTypeCode);
    }

    public CompletableFuture<ApplicationUid> prefetch(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        final CompletableFuture<ApplicationUid> future = applicationUidCacheService.asyncGetOrCreateApplicationUid(serviceUid, applicationName, serviceTypeCode);

        future.whenComplete((futureUid, throwable) -> {
            if (throwable != null) {
                logger.error("Failed to fetch application UID for serviceUid: {}, applicationName: {}", serviceUid, applicationName, throwable);
            } else {
                putApplicationUidToCache(serviceUid, applicationName, serviceTypeCode, futureUid);
            }
        });
        return future;
    }

    private void putApplicationUidToCache(ServiceUid serviceUid, String applicationName, int serviceTypeCode, ApplicationUid applicationUid) {
        if (this.cache != null) {
            if (!ApplicationUid.ERROR_APPLICATION_UID.equals(applicationUid)) {
                this.cache.put(serviceUid, applicationName, serviceTypeCode, applicationUid);
            }
        }
    }
}
