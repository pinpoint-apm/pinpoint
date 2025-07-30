package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static com.navercorp.pinpoint.web.uid.config.ApplicationUidCacheConfig.APPLICATION_NAME_CACHE_NAME;
import static com.navercorp.pinpoint.web.uid.config.ApplicationUidCacheConfig.APPLICATION_UID_CACHE_NAME;

@Service
@CacheConfig(cacheManager = "applicationUidCache")
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class ApplicationUidCachedServiceImpl implements ApplicationUidService {

    private final BaseApplicationUidService applicationUidService;

    public ApplicationUidCachedServiceImpl(BaseApplicationUidService baseApplicationUidService) {
        this.applicationUidService = Objects.requireNonNull(baseApplicationUidService, "baseApplicationUidService");
    }

    @Override
    public List<ApplicationUidAttribute> getApplicationNames(ServiceUid serviceUid) {
        return applicationUidService.getApplications(serviceUid);
    }

    @Override
    public List<ApplicationUid> getApplicationUid(ServiceUid serviceUid, String applicationName) {
        return applicationUidService.getApplicationUid(serviceUid, applicationName);
    }

    @Override
    @Cacheable(cacheNames = APPLICATION_UID_CACHE_NAME, unless = "#result == null")
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        return applicationUidService.getApplicationUid(serviceUid, applicationName, serviceTypeCode);
    }

    @Override
    @Cacheable(cacheNames = APPLICATION_NAME_CACHE_NAME, unless = "#result == null")
    public ApplicationUidAttribute getApplication(ServiceUid serviceUid, ApplicationUid applicationUid) {
        return applicationUidService.getApplication(serviceUid, applicationUid);
    }

    @Override
    @CacheEvict(cacheNames = APPLICATION_UID_CACHE_NAME)
    public void deleteApplication(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        applicationUidService.deleteApplication(serviceUid, applicationName, serviceTypeCode);
    }
}
