package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.config.CollectorPinpointIdCacheConfig;
import com.navercorp.pinpoint.collector.dao.ServiceUidDao;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import org.springframework.cache.annotation.Cacheable;

import java.util.Objects;

public class ServiceGroupServiceImpl implements ServiceGroupService {

    private final ServiceUidDao serviceUidDao;

    public ServiceGroupServiceImpl(ServiceUidDao serviceUidDao) {
        this.serviceUidDao = Objects.requireNonNull(serviceUidDao, "serviceUidDao");
    }

    @Override
    @Cacheable(cacheNames = "collectorServiceUidCache", key = "#serviceName", cacheManager = CollectorPinpointIdCacheConfig.SERVICE_UID_CACHE_NAME, unless = "#result == null")
    public ServiceUid getServiceUid(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");
        return serviceUidDao.selectServiceUid(serviceName);
    }

}
