package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.service.component.StaticServiceRegistry;
import com.navercorp.pinpoint.service.service.ServiceInfoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.navercorp.pinpoint.collector.uid.config.ServiceUidMysqlCacheConfig.SERVICE_UID_CACHE_NAME;

@Service
public class ServiceUidService {

    private final StaticServiceRegistry registry;
    private final ServiceInfoService serviceInfoService;
    private final Cache serviceUidCache;

    public ServiceUidService(StaticServiceRegistry staticServiceRegistry,
                             ServiceInfoService serviceInfoService,
                             @Qualifier("serviceUidCache") CacheManager serviceUidCacheManager) {
        this.registry = Objects.requireNonNull(staticServiceRegistry, "staticServiceRegistry");
        this.serviceInfoService = Objects.requireNonNull(serviceInfoService, "serviceService");
        Objects.requireNonNull(serviceUidCacheManager, "serviceUidCacheManager");
        this.serviceUidCache = Objects.requireNonNull(serviceUidCacheManager.getCache(SERVICE_UID_CACHE_NAME), "serviceUidCache");
    }

    public ServiceUid getServiceUid(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");
        ServiceUid staticServiceUid = registry.getServiceUid(serviceName);
        if (staticServiceUid != null) {
            return staticServiceUid;
        }

        return getUsingCache(serviceName);
    }

    // handle missing cases using ServiceUid.NULL
    private ServiceUid getUsingCache(String serviceName) {
        ServiceUid cachedResult = serviceUidCache.get(serviceName,
                () -> Objects.requireNonNullElse(serviceInfoService.getServiceUid(serviceName), ServiceUid.NULL));
        if (ServiceUid.NULL.equals(cachedResult)) {
            return null;
        }
        return cachedResult;
    }
}
