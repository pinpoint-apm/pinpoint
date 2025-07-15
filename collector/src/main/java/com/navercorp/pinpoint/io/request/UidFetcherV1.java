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

    private final UidCache cache;

    private volatile CompletableFuture<ApplicationUid> applicationUidFuture;

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

        final CompletableFuture<ApplicationUid> copy = this.applicationUidFuture;
        if (copy != null) {
            return UidSuppliers.of(applicationName, copy);
        }

        if (cache != null) {
            final ApplicationUid cachedUid = getApplicationUidFromCache(cache, serviceUid, applicationName);
            if (cachedUid != null) {
                return UidSuppliers.of(applicationName, cachedUid);
            }
        }
        CompletableFuture<ApplicationUid> prefetch = prefetch(serviceUid, applicationName);
        return UidSuppliers.of(applicationName, prefetch);
    }

    private ApplicationUid getApplicationUidFromCache(UidCache cache, ServiceUid serviceUid, String applicationName) {
        return cache.getApplicationUid(serviceUid, applicationName);
    }

    public CompletableFuture<ApplicationUid> prefetch(ServiceUid serviceUid, String applicationName) {
        lock.lock();
        try {
            CompletableFuture<ApplicationUid> copy = this.applicationUidFuture;
            if (copy != null) {
                // already fetched
                return copy;
            } else {
                CompletableFuture<ApplicationUid> future = fetchApplicationUidAsync(serviceUid, applicationName);
                this.applicationUidFuture = future;
                future.thenAccept(applicationUid -> putApplicationUidToCache(serviceUid, applicationName, applicationUid));
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
        return this.applicationUidCacheService.asyncGetOrCreateApplicationUid(serviceUid, applicationName)
                .thenApply((applicationUid) -> {
                    if (applicationUid == null) {
                        throw new IllegalStateException("ApplicationUid is null for applicationName: " + applicationName);
                    }
                    return applicationUid;
                });
    }
}
