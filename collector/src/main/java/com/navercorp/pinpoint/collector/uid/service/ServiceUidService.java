package com.navercorp.pinpoint.collector.uid.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.service.component.StaticServiceRegistry;
import com.navercorp.pinpoint.service.service.ServiceInfoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class ServiceUidService {

    private final StaticServiceRegistry registry;
    private final ServiceInfoService serviceInfoService;
    private final Cache<String, ServiceUid> serviceUidCache;

    public ServiceUidService(StaticServiceRegistry staticServiceRegistry,
                             ServiceInfoService serviceInfoService,
                             @Qualifier("serviceUidCache") Optional<Cache<String, ServiceUid>> optionalCache) {
        this.registry = Objects.requireNonNull(staticServiceRegistry, "staticServiceRegistry");
        this.serviceInfoService = Objects.requireNonNull(serviceInfoService, "serviceService");
        this.serviceUidCache = optionalCache.orElse(null);
    }

    public ServiceUid getServiceUid(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");
        ServiceUid staticServiceUid = registry.getServiceUid(serviceName);
        if (staticServiceUid != null) {
            return staticServiceUid;
        }

        return getOrLoadServiceUid(serviceName);
    }

    private ServiceUid getOrLoadServiceUid(String serviceName) {
        ServiceUid cachedResult = cacheGet(serviceName);
        if (cachedResult != null) {
            return cachedResult;
        }

        ServiceUid newServiceUid = serviceInfoService.getServiceUid(serviceName);
        cachePut(serviceName, newServiceUid);
        return newServiceUid;
    }

    private ServiceUid cacheGet(String serviceName) {
        if (serviceUidCache == null) {
            return null;
        }
        return serviceUidCache.getIfPresent(serviceName);
    }

    private void cachePut(String serviceName, ServiceUid serviceUid) {
        if (serviceUidCache == null || serviceUid == null) {
            return;
        }
        serviceUidCache.put(serviceName, serviceUid);
    }
}
