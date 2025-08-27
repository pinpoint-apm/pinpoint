package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class UidExponentialBackoffFetcherV1 implements UidFetcher {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private static final ServiceUid DEFAULT_SERVICE_UID = ServiceUid.DEFAULT;

    private final ApplicationUidService applicationUidCacheService;
    private final UidCache cache;

    private int failedCount = 0;
    private long allowedTime = 0;

    public UidExponentialBackoffFetcherV1(ApplicationUidService applicationUidCacheService, UidCache cache) {
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
        if (isBackoffInEffect()) {
            return CompletableFuture.failedFuture(new RuntimeException("Backoff in effect. Next allowed time: " + allowedTime));
        }

        return prefetch(serviceUid, applicationName, serviceTypeCode);
    }

    private CompletableFuture<ApplicationUid> prefetch(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        final CompletableFuture<ApplicationUid> future = applicationUidCacheService.asyncGetOrCreateApplicationUid(serviceUid, applicationName, serviceTypeCode);
        int currentFailedCount = failedCount;
        future.whenComplete((futureUid, throwable) -> {
            if (throwable != null) {
                onFailure(currentFailedCount);
                logger.error("Failed to fetch application UID for serviceUid: {}, applicationName: {}", serviceUid, applicationName, throwable);
            } else {
                onSuccess(serviceUid, applicationName, serviceTypeCode, futureUid);
            }
        });
        return future;
    }

    private boolean isBackoffInEffect() {
        return allowedTime > 0 && allowedTime > System.currentTimeMillis();
    }

    private long calculateNextAllowedTime(int failedCount, int minAttemptsWithoutDelay, long baseInterval, long maxInterval) {
        if (failedCount <= minAttemptsWithoutDelay) {
            return 0;
        }

        int effectiveCount = Math.min(failedCount - minAttemptsWithoutDelay, 5);
        return System.currentTimeMillis() + Math.min(baseInterval * (1L << (effectiveCount - 1)), maxInterval);
    }

    private void onFailure(int currentFailedCount) {
        if (failedCount == currentFailedCount) {
            failedCount = currentFailedCount + 1;
            allowedTime = calculateNextAllowedTime(currentFailedCount + 1, 2, 200L, 2000L);
        }
    }

    private void onSuccess(ServiceUid serviceUid, String applicationName, int serviceTypeCode, ApplicationUid applicationUid) {
        if (this.cache != null) {
            if (!ApplicationUid.ERROR_APPLICATION_UID.equals(applicationUid)) {
                this.cache.put(serviceUid, applicationName, serviceTypeCode, applicationUid);
            }
        }
        failedCount = 0;
    }
}
