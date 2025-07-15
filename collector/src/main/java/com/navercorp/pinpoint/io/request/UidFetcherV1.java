package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.io.request.supplier.UidSuppliers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class UidFetcherV1 implements UidFetcher {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private static final ServiceUid DEFAULT_SERVICE_UID = ServiceUid.DEFAULT;

    private final ApplicationUidService applicationUidCacheService;
    private final UidCache cache;

    private final ReentrantLock lock = new ReentrantLock();
    private volatile ApplicationUid applicationUid;
    private volatile CompletableFuture<ApplicationUid> applicationUidFuture;

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

        if (cache != null) {
            final ApplicationUid cachedUid = cache.getApplicationUid(serviceUid, applicationName);
            if (cachedUid != null) {
                this.applicationUid = cachedUid;
                return UidSuppliers.of(applicationName, cachedUid);
            }
        }

        CompletableFuture<ApplicationUid> prefetch = prefetch(serviceUid, applicationName);
        return UidSuppliers.of(applicationName, prefetch);
    }

    public CompletableFuture<ApplicationUid> prefetch(ServiceUid serviceUid, String applicationName) {
        CompletableFuture<ApplicationUid> copy = this.applicationUidFuture;
        if (copy != null) {
            return copy;
        }
        lock.lock();
        try {
            copy = this.applicationUidFuture;
            if (copy != null) {
                return copy;
            } else {
                final CompletableFuture<ApplicationUid> future = fetchApplicationUidAsync(serviceUid, applicationName);
                this.applicationUidFuture = future;

                future.whenComplete((futureUid, throwable) -> {
                    if (throwable != null) {
                        if (applicationUidFuture == future) {
                            applicationUidFuture = null;
                        }
                        logger.error("Failed to fetch application UID for serviceUid: {}, applicationName: {}", serviceUid, applicationName, throwable);
                    } else {
                        this.applicationUid = futureUid;
                        putApplicationUidToCache(serviceUid, applicationName, futureUid);
                    }
                });
                return future;
            }
        } finally {
            lock.unlock();
        }
    }

    private void putApplicationUidToCache(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid) {
        if (this.cache != null) {
            if (!ApplicationUid.ERROR_APPLICATION_UID.equals(applicationUid)) {
                this.cache.put(serviceUid, applicationName, applicationUid);
            }
        }
    }

    private CompletableFuture<ApplicationUid> fetchApplicationUidAsync(ServiceUid serviceUid, String applicationName) {
        return this.applicationUidCacheService.asyncGetOrCreateApplicationUid(serviceUid, applicationName);
    }
}
