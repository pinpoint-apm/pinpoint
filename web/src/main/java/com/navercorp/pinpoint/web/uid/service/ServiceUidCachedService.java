package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.service.component.StaticServiceRegistry;
//import com.navercorp.pinpoint.service.service.ServiceInfoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.navercorp.pinpoint.web.uid.config.ServiceUidMysqlCacheConfig.SERVICE_NAME_CACHE_NAME;
import static com.navercorp.pinpoint.web.uid.config.ServiceUidMysqlCacheConfig.SERVICE_UID_CACHE_NAME;

@Deprecated
@Service
@CacheConfig(cacheManager = "serviceUidCache")
public class ServiceUidCachedService implements ServiceUidService {

    private final Logger logger = LogManager.getLogger(this.getClass());
//    private final StaticServiceRegistry registry;
//    private final ServiceInfoService serviceInfoService;
//
//    public ServiceUidCachedService(StaticServiceRegistry staticServiceRegistry,
//                                   ServiceInfoService serviceInfoService) {
//        this.registry = Objects.requireNonNull(staticServiceRegistry, "staticServiceRegistry");
//        this.serviceInfoService = Objects.requireNonNull(serviceInfoService, "serviceService");
//    }


    @Override
//    @Cacheable(cacheNames = SERVICE_UID_CACHE_NAME, unless = "#result == null")
    public ServiceUid getServiceUid(String serviceName) {
//        Objects.requireNonNull(serviceName, "serviceName");
//        ServiceUid staticServiceUid = registry.getServiceUid(serviceName);
//        if (staticServiceUid != null) {
//            return staticServiceUid;
//        }
//
//        return serviceInfoService.getServiceUid(serviceName);
        //Not implemented yet
        logger.warn("getServiceUid is not implemented yet. Returning default value. serviceName={}", serviceName);
        return ServiceUid.DEFAULT;
    }

    @Override
//    @Cacheable(cacheNames = SERVICE_NAME_CACHE_NAME, unless = "#result == null")
    public String getServiceName(ServiceUid serviceUid) {
//        Objects.requireNonNull(serviceUid, "serviceUid");
//        String staticServiceName = registry.getServiceName(serviceUid);
//        if (staticServiceName != null) {
//            return staticServiceName;
//        }
//
        //Not implemented yet
        logger.warn("getServiceUid is not implemented yet. Returning default value. serviceUid={}", serviceUid);
        return ServiceUid.DEFAULT_SERVICE_UID_NAME;
    }

//    @CacheEvict(cacheNames = SERVICE_UID_CACHE_NAME)
    public void serviceUidCacheEvict(String serviceName) {
//        Objects.requireNonNull(serviceName, "serviceName");
        throw new UnsupportedOperationException("Not implemented yet");
    }

//    @CacheEvict(cacheNames = SERVICE_NAME_CACHE_NAME)
    public void serviceNameCacheEvict(ServiceUid serviceUid) {
//        Objects.requireNonNull(serviceUid, "serviceUid");
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
