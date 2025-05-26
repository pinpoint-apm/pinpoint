package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.uid.service.async.AsyncApplicationUidService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKeyGenerator;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.navercorp.pinpoint.collector.uid.config.CollectorApplicationUidConfig.APPLICATION_UID_CACHE_NAME;

public class CachedApplicationUidServiceImpl implements CachedApplicationUidService {

    private final ApplicationUidService applicationUidService;
    private final AsyncApplicationUidService asyncApplicationUidService;

    private final Cache applicationUidCache;

    public CachedApplicationUidServiceImpl(ApplicationUidService applicationUidService,
                                           AsyncApplicationUidService asyncApplicationUidService,
                                           @Qualifier(APPLICATION_UID_CACHE_NAME) CacheManager cacheManager) {
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "baseApplicationUidService");
        this.asyncApplicationUidService = Objects.requireNonNull(asyncApplicationUidService, "asyncApplicationUidService");
        this.applicationUidCache = Objects.requireNonNull(cacheManager, "cacheManager").getCache("applicationUidCache");
    }

    protected Object createSimpleCacheKey(Object... params) {
        return SimpleKeyGenerator.generateKey(params);
    }

    @Override
    @Cacheable(cacheNames = "applicationUidCache", cacheManager = APPLICATION_UID_CACHE_NAME, unless = "#result == null")
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName) {
        return applicationUidService.getApplicationUid(serviceUid, applicationName);
    }

    @Override
    @Cacheable(cacheNames = "applicationUidCache", cacheManager = APPLICATION_UID_CACHE_NAME, unless = "#result == null")
    public ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName) {
        return applicationUidService.getOrCreateApplicationUid(serviceUid, applicationName);
    }

    @Override
    public CompletableFuture<ApplicationUid> asyncGetOrCreateApplicationUid(ServiceUid serviceUid, String applicationName) {
        return applicationUidCache.retrieve(createSimpleCacheKey(serviceUid, applicationName),
                () -> asyncApplicationUidService.getOrCreateApplicationUid(serviceUid, applicationName));
    }
}
