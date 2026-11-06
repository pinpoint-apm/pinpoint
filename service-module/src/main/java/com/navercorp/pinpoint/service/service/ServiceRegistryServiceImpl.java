package com.navercorp.pinpoint.service.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.IdGenerator;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.service.dao.ServiceRegistryDao;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.service.dao.dto.ServiceEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Service
public class ServiceRegistryServiceImpl implements ServiceRegistryService {

    private final ServiceRegistryDao serviceRegistryDao;
    private final IdGenerator<ServiceUid> serviceUidGenerator;
    private final List<ServiceDeletionListener> serviceDeletionListeners;

    public ServiceRegistryServiceImpl(ServiceRegistryDao serviceRegistryDao,
                                      IdGenerator<ServiceUid> serviceUidGenerator) {
        this(serviceRegistryDao, serviceUidGenerator, List.of());
    }

    @Autowired
    public ServiceRegistryServiceImpl(ServiceRegistryDao serviceRegistryDao,
                                      IdGenerator<ServiceUid> serviceUidGenerator,
                                      @Lazy List<ServiceDeletionListener> serviceDeletionListeners) {
        this.serviceRegistryDao = Objects.requireNonNull(serviceRegistryDao, "serviceRegistryDao");
        this.serviceUidGenerator = Objects.requireNonNull(serviceUidGenerator, "serviceUidGenerator");
        // Listeners reach back to this service through their own dependencies, so the
        // lazy list resolves them on first delete instead of at construction.
        this.serviceDeletionListeners = Objects.requireNonNull(serviceDeletionListeners, "serviceDeletionListeners");
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    @Retryable(retryFor = DuplicateKeyException.class, maxAttempts = 3)
    public Service insertService(String name) {
        StringPrecondition.requireHasLength(name, "name");
        if (Service.wellKnownService(name) != null) {
            throw new IllegalArgumentException("name is well known");
        }

        int uid = serviceUidGenerator.generate().getUid();
        try {
            serviceRegistryDao.insertService(uid, name);
        } catch (DuplicateKeyException e) {
            if (serviceRegistryDao.selectServiceByName(name) != null) {
                throw new DataIntegrityViolationException("Duplicate service name: " + name, e);
            }
            throw e;
        }

        return new Service(name, uid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getServiceNames() {
        return serviceRegistryDao.selectServiceNames();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> getServiceList(int limit) {
        List<ServiceEntity> entityList = serviceRegistryDao.selectServiceList(limit);
        return toServices(entityList);
    }

    private @NonNull List<Service> toServices(List<ServiceEntity> entityList) {
        List<Service> serviceList = new ArrayList<>(entityList.size());
        for (ServiceEntity entity : entityList) {
            // non null
            Service service = new Service(entity.getName(), entity.getUid());
            serviceList.add(service);
        }
        return serviceList;
    }

    @Override
    @Transactional(readOnly = true)
    public Service getService(String name) {
        Objects.requireNonNull(name, "name");
        return toService(serviceRegistryDao.selectServiceByName(name));
    }

    @Override
    @Transactional(readOnly = true)
    public Service getService(int uid) {
        return toService(serviceRegistryDao.selectServiceByUid(uid));
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void deleteService(String name) {
        Objects.requireNonNull(name, "name");
        ServiceEntity service = serviceRegistryDao.selectServiceByName(name);
        if (service == null) {
            return;
        }
        Service deleted = toService(service);
        for (ServiceDeletionListener listener : serviceDeletionListeners) {
            listener.onServiceDelete(deleted);
        }
        serviceRegistryDao.deleteService(service.getUid());
    }

    private static Service toService(ServiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Service(entity.getName(), entity.getUid());
    }
}
