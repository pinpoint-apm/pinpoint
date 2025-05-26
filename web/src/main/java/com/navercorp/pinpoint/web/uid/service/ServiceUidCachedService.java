package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.service.component.StaticServiceRegistry;
import com.navercorp.pinpoint.service.service.ServiceInfoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.navercorp.pinpoint.web.uid.config.ServiceUidMysqlCacheConfig.SERVICE_NAME_CACHE_NAME;
import static com.navercorp.pinpoint.web.uid.config.ServiceUidMysqlCacheConfig.SERVICE_UID_CACHE_NAME;

@Service
@CacheConfig(cacheManager = "serviceUidCache")
public class ServiceUidCachedService {

    private final StaticServiceRegistry registry;
    private final ServiceInfoService serviceInfoService;

    public ServiceUidCachedService(StaticServiceRegistry staticServiceRegistry,
                                   ServiceInfoService serviceInfoService,
                                   @Qualifier("serviceUidCache") CacheManager serviceUidCacheManager) {
        this.registry = Objects.requireNonNull(staticServiceRegistry, "staticServiceRegistry");
        this.serviceInfoService = Objects.requireNonNull(serviceInfoService, "serviceService");
    }


    @Cacheable(cacheNames = SERVICE_UID_CACHE_NAME, unless = "#result == null")
    public ServiceUid getServiceUid(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");
        ServiceUid staticServiceUid = registry.getServiceUid(serviceName);
        if (staticServiceUid != null) {
            return staticServiceUid;
        }

        return serviceInfoService.getServiceUid(serviceName);
    }

    @Cacheable(cacheNames = SERVICE_NAME_CACHE_NAME, unless = "#result == null")
    public String getServiceName(ServiceUid serviceUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        String staticServiceName = registry.getServiceName(serviceUid);
        if (staticServiceName != null) {
            return staticServiceName;
        }

        return serviceInfoService.getServiceName(serviceUid);
    }

    @CacheEvict(cacheNames = SERVICE_UID_CACHE_NAME)
    public void serviceUidCacheEvict(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");
    }

    @CacheEvict(cacheNames = SERVICE_NAME_CACHE_NAME)
    public void serviceNameCacheEvict(ServiceUid serviceUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
    }
}
