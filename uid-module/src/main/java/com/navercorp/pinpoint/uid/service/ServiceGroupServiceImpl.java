package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.uid.dao.ServiceNameDao;
import com.navercorp.pinpoint.uid.dao.ServiceUidDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

public class ServiceGroupServiceImpl implements ServiceGroupService {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServiceUidDao serviceUidDao;
    private final ServiceNameDao serviceNameDao;
    private final IdGenerator<ServiceUid> serviceUidGenerator;

    public ServiceGroupServiceImpl(ServiceUidDao serviceUidDao, ServiceNameDao serviceNameDao, IdGenerator<ServiceUid> serviceUidGenerator) {
        this.serviceUidDao = Objects.requireNonNull(serviceUidDao, "serviceUidDao");
        this.serviceNameDao = Objects.requireNonNull(serviceNameDao, "serviceNameDao");
        this.serviceUidGenerator = Objects.requireNonNull(serviceUidGenerator, "serviceUidGenerator");
    }

    @Override
    public List<String> selectAllServiceNames() {
        return serviceUidDao.selectAllServiceNames();
    }

    @Override
    public String selectServiceName(ServiceUid serviceUid) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        return serviceNameDao.selectServiceName(serviceUid);
    }

    @Override
    public ServiceUid getServiceUid(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");
        return serviceUidDao.selectServiceUid(serviceName);
    }

    @Override
    public void createService(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");

        ServiceUid serviceUid = serviceUidDao.selectServiceUid(serviceName);
        if (serviceUid != null) {
            throw new IllegalStateException("already existing serviceName: " + serviceName);
        }
        createNewService(serviceName);
    }

    private void createNewService(String serviceName) {
        ServiceUid newServiceUid = insertServiceNameWithRetries(serviceName, 3);
        if (newServiceUid == null) {
            throw new IllegalStateException("serviceUid collision try again");
        }
        logger.info("saved (id:{} -> name:{})", newServiceUid, serviceName);

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

    private ServiceUid insertServiceNameWithRetries(String serviceName, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            ServiceUid newServiceUid = serviceUidGenerator.generate();

            boolean isSuccess = serviceNameDao.insertServiceNameIfNotExists(newServiceUid, serviceName);
            if (isSuccess) {
                return newServiceUid;
            }
        }
        logger.error("ServiceUid collision occurred. serviceName: {}, maxRetries: {}", serviceName, maxRetries);
        return null;
    }

    @Override
    public void deleteService(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName");

        ServiceUid serviceUid = serviceUidDao.selectServiceUid(serviceName);
        serviceUidDao.deleteServiceUid(serviceName);
        logger.info("deleted (name:{} -> id:{})", serviceName, serviceUid);
        if (serviceUid != null) {
            serviceNameDao.deleteServiceName(serviceUid);
            logger.info("deleted (id:{} -> name:{})", serviceUid, serviceName);
        }
    }

}
