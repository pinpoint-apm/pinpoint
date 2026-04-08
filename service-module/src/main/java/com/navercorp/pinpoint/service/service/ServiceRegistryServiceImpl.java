package com.navercorp.pinpoint.service.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.service.dao.ServiceRegistryDao;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ServiceRegistryServiceImpl implements ServiceRegistryService {

    private final ServiceRegistryDao serviceRegistryDao;
    private final IdGenerator<ServiceUid> serviceUidGenerator;

    public ServiceRegistryServiceImpl(ServiceRegistryDao serviceRegistryDao,
                                      IdGenerator<ServiceUid> serviceUidGenerator) {
        this.serviceRegistryDao = Objects.requireNonNull(serviceRegistryDao, "serviceRegistryDao");
        this.serviceUidGenerator = Objects.requireNonNull(serviceUidGenerator, "serviceUidGenerator");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    @Retryable(retryFor = DuplicateKeyException.class, maxAttempts = 3)
    public ServiceEntity insertService(String name) {
        Objects.requireNonNull(name, "name");
        int uid = serviceUidGenerator.generate().getUid();
        serviceRegistryDao.insertService(uid, name);

        ServiceEntity entity = new ServiceEntity();
        entity.setUid(uid);
        entity.setName(name);
        return entity;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getServiceNames() {
        return serviceRegistryDao.selectServiceNames();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceEntity getService(String name) {
        Objects.requireNonNull(name, "name");
        return serviceRegistryDao.selectService(name);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void deleteService(String name) {
        Objects.requireNonNull(name, "name");
        ServiceEntity service = serviceRegistryDao.selectService(name);
        if (service == null) {
            return;
        }
        serviceRegistryDao.deleteService(service.getUid());
    }
}
