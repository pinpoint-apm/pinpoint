package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKeyGenerator;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.navercorp.pinpoint.collector.uid.config.CollectorApplicationUidConfig.APPLICATION_UID_CACHE_NAME;

public class ApplicationUidServiceImpl implements ApplicationUidService {

    private final BaseApplicationUidService baseApplicationUidService;

    private final Cache applicationUidCache;

    public ApplicationUidServiceImpl(BaseApplicationUidService baseApplicationUidService,
                                     @Qualifier(APPLICATION_UID_CACHE_NAME) CacheManager cacheManager) {
        this.baseApplicationUidService = Objects.requireNonNull(baseApplicationUidService, "baseApplicationUidService");
        this.applicationUidCache = Objects.requireNonNull(cacheManager, "cacheManager").getCache("applicationUidCache");
    }

    protected Object createSimpleCacheKey(Object... params) {
        return SimpleKeyGenerator.generateKey(params);
    }

    @Override
    @Cacheable(cacheNames = "applicationUidCache", cacheManager = APPLICATION_UID_CACHE_NAME, unless = "#result == null")
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName) {
        return baseApplicationUidService.getApplicationUid(serviceUid, applicationName);
    }

    @Override
    @Cacheable(cacheNames = "applicationUidCache", cacheManager = APPLICATION_UID_CACHE_NAME, unless = "#result == null")
    public ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName) {
        return baseApplicationUidService.getOrCreateApplicationUid(serviceUid, applicationName);
    }

    @Override
    public CompletableFuture<ApplicationUid> asyncGetOrCreateApplicationUid(ServiceUid serviceUid, String applicationName) {
        return applicationUidCache.retrieve(createSimpleCacheKey(serviceUid, applicationName),
                () -> baseApplicationUidService.asyncGetOrCreateApplicationUid(serviceUid, applicationName));
    }
}
