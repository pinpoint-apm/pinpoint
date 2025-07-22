package com.navercorp.pinpoint.service.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.service.dao.ServiceDao;
import com.navercorp.pinpoint.service.vo.ServiceEntry;
import com.navercorp.pinpoint.service.vo.ServiceInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional(rollbackFor = {Exception.class})
public class ServiceInfoServiceImpl implements ServiceInfoService {

    private final ServiceDao serviceDao;
    private final IdGenerator<ServiceUid> serviceUidGenerator;

    public ServiceInfoServiceImpl(ServiceDao serviceDao, @Qualifier("serviceUidGenerator") IdGenerator<ServiceUid> serviceUidGenerator) {
        this.serviceDao = Objects.requireNonNull(serviceDao, "serviceDao");
        this.serviceUidGenerator = serviceUidGenerator;
    }

    @Override
    @Retryable(retryFor = DuplicateKeyException.class, maxAttempts = 3)
    public int insertService(String serviceName, Map<String, String> configuration) {
        List<String> existingServiceNames = serviceDao.selectServiceNames();
        if (existingServiceNames.contains(serviceName)) {
            throw new IllegalArgumentException("Duplicate service name: " + serviceName);
        }
        ServiceUid uid = serviceUidGenerator.generate();
        return serviceDao.insertService(uid.getUid(), serviceName, configuration);
    }

    @Override
    public List<String> getServiceNames() {
        return serviceDao.selectServiceNames();
    }

    @Override
    public ServiceInfo getServiceInfo(String serviceName) {
        return serviceDao.selectServiceInfo(serviceName);
    }

    @Override
    public ServiceUid getServiceUid(String serviceName) {
        ServiceEntry serviceEntry = serviceDao.selectServiceEntry(serviceName);
        if (serviceEntry != null) {
            return ServiceUid.of(serviceEntry.getUid());
        }
        return null;
    }

    @Override
    public String getServiceName(ServiceUid serviceUid) {
        ServiceEntry serviceEntry = serviceDao.selectServiceEntry(serviceUid.getUid());
        if (serviceEntry != null) {
            return serviceEntry.getName();
        }
        return null;
    }

    @Override
    public void updateServiceConfig(String serviceName, Map<String, String> newConfiguration) {
        ServiceInfo serviceInfo = serviceDao.selectServiceInfo(serviceName);
        if (serviceInfo == null) {
            throw new IllegalArgumentException("Service not found: " + serviceName);
        }

        serviceDao.updateServiceConfig(serviceInfo.getUid(), newConfiguration);
    }

    @Override
    public void updateServiceName(String serviceName, String newServiceName) {
        ServiceInfo serviceInfo = serviceDao.selectServiceInfo(serviceName);
        if (serviceInfo == null) {
            throw new IllegalArgumentException("Service not found: " + serviceName);
        }
        serviceDao.updateServiceName(serviceInfo.getUid(), newServiceName);
    }

    @Override
    public void deleteService(String serviceName) {
        ServiceEntry serviceEntry = serviceDao.selectServiceEntry(serviceName);
        if (serviceEntry != null) {
            serviceDao.deleteService(serviceEntry.getUid());
        }
    }

    public void deleteService(int uid) {
        serviceDao.deleteService(uid);
    }
}
