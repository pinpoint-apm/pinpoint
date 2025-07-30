package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.navercorp.pinpoint.collector.uid.config.ApplicationUidCacheConfig.APPLICATION_UID_CACHE_NAME;

@Service
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class ApplicationUidCachedServiceImpl implements ApplicationUidService {

    private final BaseApplicationUidService baseApplicationUidService;

    private final Cache applicationUidCache;

    public ApplicationUidCachedServiceImpl(BaseApplicationUidService baseApplicationUidService,
                                           @Qualifier("applicationUidCache") CacheManager cacheManager) {
        this.baseApplicationUidService = Objects.requireNonNull(baseApplicationUidService, "baseApplicationUidService");
        this.applicationUidCache = Objects.requireNonNull(cacheManager, "cacheManager").getCache("applicationUidCache");
    }

    @Override
    @Cacheable(cacheNames = APPLICATION_UID_CACHE_NAME, cacheManager = "applicationUidCache", unless = "#result == null")
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        return baseApplicationUidService.getApplicationUid(serviceUid, applicationName, serviceTypeCode);
    }

    @Override
    public ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        return asyncGetOrCreateApplicationUid(serviceUid, applicationName, serviceTypeCode).join();
    }

    // throw CompletionException if valueLoader throws an exception
    @Override
    public CompletableFuture<ApplicationUid> asyncGetOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        return applicationUidCache.retrieve(SimpleKeyGenerator.generateKey(serviceUid, applicationName, serviceTypeCode),
                () -> baseApplicationUidService.asyncGetOrCreateApplicationUid(serviceUid, applicationName, serviceTypeCode));
    }
}
