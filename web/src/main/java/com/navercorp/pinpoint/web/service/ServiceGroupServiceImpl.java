package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.ServiceNameDao;
import com.navercorp.pinpoint.web.dao.ServiceTagDao;
import com.navercorp.pinpoint.web.dao.ServiceUidDao;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ServiceGroupServiceImpl implements ServiceGroupService {
    private final ServiceUidDao serviceUidDao;
    private final ServiceNameDao serviceNameDao;
    private final ServiceTagDao serviceTagDao;

    private final Logger logger = LogManager.getLogger(this.getClass());

    public ServiceGroupServiceImpl(ServiceUidDao serviceUidDao, ServiceNameDao serviceNameDao, ServiceTagDao serviceTagDao) {
        this.serviceUidDao = Objects.requireNonNull(serviceUidDao, "serviceUidDao");
        this.serviceNameDao = Objects.requireNonNull(serviceNameDao, "serviceNameDao");
        this.serviceTagDao = Objects.requireNonNull(serviceTagDao, "serviceInfoDao");
    }

    @Override
    public void createServiceGroup(String serviceName, Map<String, String> tags) {
        // 1. insert (id -> name, info)
        UUID newServiceUid = insertServiceInfoWithRetries(serviceName, 3);
        if (newServiceUid != null) {
            logger.info("saved (id:{} -> name:{})", newServiceUid, serviceName);
        } else {
            throw new IllegalStateException("Failed to create new serviceUid. serviceName: " + serviceName);
        }

        // 2. insert (name -> id)
        try {
            boolean insertResult = serviceUidDao.insertServiceUidIfNotExists(serviceName, newServiceUid);
            if (insertResult) {
                logger.info("saved (name:{} -> id:{})", serviceName, newServiceUid);
                serviceTagDao.insertServiceTag(newServiceUid, tags);
            } else {
                throw new IllegalStateException("already existing serviceName: " + serviceName);
            }
        } catch (Exception e) {
            serviceNameDao.deleteServiceName(newServiceUid);
            logger.error("failed to save (name:{} -> id:{})", serviceName, newServiceUid, e);
            throw e;
        }
    }

    private UUID insertServiceInfoWithRetries(String serviceName, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            UUID newServiceUid = UUID.randomUUID();

            boolean isSuccess = serviceNameDao.insertServiceNameIfNotExists(newServiceUid, serviceName);
            if (isSuccess) {
                return newServiceUid;
            }
        }
        logger.error("UUID collision occurred. serviceName: {}, maxRetries: {}", serviceName, maxRetries);
        return null;
    }

    @Override
    public void deleteServiceGroup(String serviceName) {
        UUID serviceUid = selectServiceUid(serviceName);
        serviceUidDao.deleteServiceUid(serviceName);
        if (serviceUid != null) {
            serviceNameDao.deleteServiceName(serviceUid);
            serviceTagDao.deleteAllServiceTags(serviceUid);
        }
    }


    @Override
    public List<String> selectAllServiceNames() {
        return serviceNameDao.selectAllServiceNames();
    }

    @Override
    public String selectServiceName(UUID serviceUid) {
        if (serviceUid == null) {
            return null;
        }
        return serviceNameDao.selectServiceName(serviceUid);
    }

    @Override
    public UUID selectServiceUid(String serviceName) {
        return serviceUidDao.selectServiceUid(serviceName);
    }


    @Override
    public Map<String, String> selectServiceTags(String serviceName) {
        UUID serviceUid = selectServiceUid(serviceName);
        if (serviceUid == null) {
            return null;
        }
        return serviceTagDao.selectServiceTags(serviceUid);
    }

    @Override
    public void insertServiceTag(String serviceName, String key, String value) {
        UUID serviceUid = selectServiceUid(serviceName);
        if (serviceUid == null) {
            throw new IllegalArgumentException("serviceUid not found. serviceName: " + serviceName);
        }

        serviceTagDao.insertServiceTag(serviceUid, key, value);
    }

    @Override
    public void deleteServiceTag(String serviceName, String key) {
        UUID serviceUid = selectServiceUid(serviceName);
        if (serviceUid == null) {
            throw new IllegalArgumentException("serviceUid not found. serviceName: " + serviceName);
        }

        serviceTagDao.deleteServiceTag(serviceUid, key);
    }
}
