package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.ApplicationUidService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Objects;

import static com.navercorp.pinpoint.web.uid.config.WebApplicationUidConfig.APPLICATION_NAME_CACHE_NAME;
import static com.navercorp.pinpoint.web.uid.config.WebApplicationUidConfig.APPLICATION_UID_CACHE_NAME;

public class CachedApplicationUidServiceImpl implements CachedApplicationUidService {

    private final ApplicationUidService applicationUidService;

    public CachedApplicationUidServiceImpl(ApplicationUidService applicationUidService) {
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "applicationUidService");
    }

    @Override
    @Cacheable(cacheNames = "applicationUidCache", cacheManager = APPLICATION_UID_CACHE_NAME, unless = "#result == null")
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName) {
        return applicationUidService.getApplicationUid(serviceUid, applicationName);
    }

    @Override
    @Cacheable(cacheNames = "applicationUidCache", cacheManager = APPLICATION_NAME_CACHE_NAME, unless = "#result == null")
    public String getApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        return applicationUidService.getApplicationName(serviceUid, applicationUid);
    }

    @Override
    @CacheEvict(cacheNames = "applicationUidCache", cacheManager = APPLICATION_UID_CACHE_NAME)
    public void deleteApplication(ServiceUid serviceUid, String applicationName) {
        applicationUidService.deleteApplication(serviceUid, applicationName);
    }
}
