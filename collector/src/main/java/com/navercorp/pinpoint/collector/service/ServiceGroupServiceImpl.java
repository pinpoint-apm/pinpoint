package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.dao.ServiceUidDao;

import java.util.Objects;
import java.util.UUID;

public class ServiceGroupServiceImpl implements ServiceGroupService {

    private final ServiceUidDao serviceUidDao;

    public ServiceGroupServiceImpl(ServiceUidDao serviceUidDao) {
        this.serviceUidDao = Objects.requireNonNull(serviceUidDao, "serviceUidDao");
    }

    @Override
    public UUID getServiceUid(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");
        return serviceUidDao.selectServiceUid(serviceName);
    }

}
