package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import com.navercorp.pinpoint.web.vo.Service;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ServiceModelResolver {

    private final ServiceRegistryService serviceRegistryService;

    public ServiceModelResolver(ServiceRegistryService serviceRegistryService) {
        this.serviceRegistryService = Objects.requireNonNull(serviceRegistryService, "serviceRegistryService");
    }

    public Service getService(int serviceUid) {
        if (Service.DEFAULT.getServiceUid() == serviceUid) {
            return Service.DEFAULT;
        }
        if (Service.TEST_SERVICE.getServiceUid() == serviceUid) {
            return Service.TEST_SERVICE;
        }
        ServiceEntity entity = serviceRegistryService.getService(serviceUid);
        if (entity == null) {
            return Service.DEFAULT;
        }

        return new Service(entity.getName(), entity.getUid());
    }

    public Service getService(String serviceName) {
        if (Service.DEFAULT.getServiceName().equals(serviceName)) {
            return Service.DEFAULT;
        }
        if (Service.TEST_SERVICE.getServiceName().equals(serviceName)) {
            return Service.TEST_SERVICE;
        }
        ServiceEntity entity = serviceRegistryService.getService(serviceName);
        if (entity == null) {
            return Service.DEFAULT;
        }

        return new Service(entity.getName(), entity.getUid());
    }
}
