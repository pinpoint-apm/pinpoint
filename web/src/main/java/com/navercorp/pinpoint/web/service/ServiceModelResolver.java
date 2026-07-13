package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import com.navercorp.pinpoint.web.vo.Service;

import java.util.Objects;

public class ServiceModelResolver {

    private final ServiceRegistryService serviceRegistryService;

    public ServiceModelResolver(ServiceRegistryService serviceRegistryService) {
        this.serviceRegistryService = Objects.requireNonNull(serviceRegistryService, "serviceRegistryService");
    }

    public Service getService(int serviceUid) {
        if (Service.DEFAULT.getServiceUid().getUid() == serviceUid) {
            return Service.DEFAULT;
        }
        if (Service.TEST_SERVICE.getServiceUid().getUid() == serviceUid) {
            return Service.TEST_SERVICE;
        }
        Service service = resolveService(serviceUid);
        if (service == null) {
            return Service.DEFAULT;
        }
        return service;
    }

    public Service getService(String serviceName) {
        if (Service.DEFAULT.getServiceName().equals(serviceName)) {
            return Service.DEFAULT;
        }
        if (Service.TEST_SERVICE.getServiceName().equals(serviceName)) {
            return Service.TEST_SERVICE;
        }
        Service service = resolveService(serviceName);
        if (service == null) {
            return Service.DEFAULT;
        }
        return service;
    }

    protected Service resolveService(int serviceUid) {
        return toService(serviceRegistryService.getService(serviceUid));
    }

    protected Service resolveService(String serviceName) {
        return toService(serviceRegistryService.getService(serviceName));
    }

    protected static Service toService(ServiceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Service(entity.getName(), entity.getUid());
    }
}
