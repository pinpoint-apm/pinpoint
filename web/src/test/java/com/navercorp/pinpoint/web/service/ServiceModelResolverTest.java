package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import com.navercorp.pinpoint.web.vo.Service;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;

class ServiceModelResolverTest {

    private final ServiceRegistryService serviceRegistryService = Mockito.mock(ServiceRegistryService.class);

    @Test
    void lookupEveryTime() {
        String serviceName = "serviceName";
        ServiceModelResolver resolver = new ServiceModelResolver(serviceRegistryService);

        Mockito.when(serviceRegistryService.getService(serviceName)).thenReturn(serviceEntity(100000, serviceName));

        assertThat(resolver.getService(serviceName)).isEqualTo(new Service(serviceName, 100000));
        assertThat(resolver.getService(serviceName)).isEqualTo(new Service(serviceName, 100000));

        Mockito.verify(serviceRegistryService, times(2)).getService(serviceName);
    }

    @Test
    void getStaticService() {
        ServiceModelResolver resolver = new ServiceModelResolver(serviceRegistryService);

        assertThat(resolver.getService(Service.DEFAULT.getServiceName())).isEqualTo(Service.DEFAULT);
        assertThat(resolver.getService(Service.DEFAULT.getServiceUid().getUid())).isEqualTo(Service.DEFAULT);
        assertThat(resolver.getService(Service.TEST_SERVICE.getServiceName())).isEqualTo(Service.TEST_SERVICE);
        assertThat(resolver.getService(Service.TEST_SERVICE.getServiceUid().getUid())).isEqualTo(Service.TEST_SERVICE);

        Mockito.verifyNoInteractions(serviceRegistryService);
    }

    @Test
    void missingServiceReturnsDefault() {
        String unRegisteredServiceName = "unRegisteredServiceName";
        ServiceModelResolver resolver = new ServiceModelResolver(serviceRegistryService);

        Mockito.when(serviceRegistryService.getService(unRegisteredServiceName)).thenReturn(null);

        assertThat(resolver.getService(unRegisteredServiceName)).isEqualTo(Service.DEFAULT);
    }

    private ServiceEntity serviceEntity(int uid, String name) {
        ServiceEntity service = new ServiceEntity();
        service.setUid(uid);
        service.setName(name);
        return service;
    }
}
