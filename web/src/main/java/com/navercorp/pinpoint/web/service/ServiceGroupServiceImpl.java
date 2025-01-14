package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.ServiceNameDao;
import com.navercorp.pinpoint.web.dao.ServiceUidDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ServiceGroupServiceImpl implements ServiceGroupService {
    private final ServiceUidDao serviceUidDao;
    private final ServiceNameDao serviceNameDao;

    private final Logger logger = LogManager.getLogger(this.getClass());

    public ServiceGroupServiceImpl(ServiceUidDao serviceUidDao, ServiceNameDao serviceNameDao) {
        this.serviceUidDao = Objects.requireNonNull(serviceUidDao, "serviceUidDao");
        this.serviceNameDao = Objects.requireNonNull(serviceNameDao, "serviceNameDao");
    }

    @Override
    public void createService(String serviceName) {
        // 1. insert (id -> name)
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
            } else {
                throw new IllegalStateException("already existing serviceName: " + serviceName);
            }
        } catch (Exception e) {
            logger.warn("save failed (name:{} -> id:{}) save failed", serviceName, newServiceUid, e);
            serviceNameDao.deleteServiceName(newServiceUid);
            logger.info("discarded (id:{} -> name:{})", newServiceUid, serviceName);
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
    public void deleteService(String serviceName) {
        UUID serviceUid =  serviceUidDao.selectServiceUid(serviceName);
        serviceUidDao.deleteServiceUid(serviceName);
        logger.info("deleted (name:{} -> id:{})", serviceName, serviceUid);
        if (serviceUid != null) {
            serviceNameDao.deleteServiceName(serviceUid);
            logger.info("deleted (id:{} -> name:{})", serviceUid, serviceName);
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
        Objects.requireNonNull(serviceName, "serviceName");
        return serviceUidDao.selectServiceUid(serviceName);
    }
}
