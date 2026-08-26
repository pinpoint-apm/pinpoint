package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import com.navercorp.pinpoint.common.server.uid.Service;

import java.util.Objects;

public class ServiceModelResolver {

    private final ServiceRegistryService serviceRegistryService;

    public ServiceModelResolver(ServiceRegistryService serviceRegistryService) {
        this.serviceRegistryService = Objects.requireNonNull(serviceRegistryService, "serviceRegistryService");
    }

    public Service getService(int serviceUid) {
        final Service wellKnown = Service.wellKnownService(serviceUid);
        if (wellKnown != null) {
            return wellKnown;
        }
        Service service = resolveService(serviceUid);
        if (service == null) {
            return Service.DEFAULT;
        }
        return service;
    }

    public Service getService(String serviceName) {
        final Service wellKnown = Service.wellKnownService(serviceName);
        if (wellKnown != null) {
            return wellKnown;
        }
        Service service = resolveService(serviceName);
        if (service == null) {
            throw new ServiceNotFoundException(serviceName);
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
