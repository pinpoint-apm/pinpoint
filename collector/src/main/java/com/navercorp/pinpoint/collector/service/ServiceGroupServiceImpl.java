package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.config.CollectorV4CacheConfig;
import com.navercorp.pinpoint.collector.dao.ServiceUidDao;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@ConditionalOnProperty(name = "pinpoint.collector.v4.enable", havingValue = "true")
public class ServiceGroupServiceImpl implements ServiceGroupService {

    private final ServiceUidDao serviceUidDao;

    public ServiceGroupServiceImpl(ServiceUidDao serviceUidDao) {
        this.serviceUidDao = Objects.requireNonNull(serviceUidDao, "serviceUidDao");
    }

    @Override
    @Cacheable(cacheNames = "collectorServiceUidCache", key = "#serviceName", cacheManager = CollectorV4CacheConfig.SERVICE_UID_CACHE_NAME, unless = "#result == null")
    public ServiceUid getServiceUid(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");
        return serviceUidDao.selectServiceUid(serviceName);
    }

}
