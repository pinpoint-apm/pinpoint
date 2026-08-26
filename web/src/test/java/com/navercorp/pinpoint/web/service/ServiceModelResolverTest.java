package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.common.server.uid.Service;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;

class ServiceModelResolverTest {

    private final ServiceRegistryService serviceRegistryService = Mockito.mock(ServiceRegistryService.class);

    @Test
    void lookupEveryTime() {
        String serviceName = "serviceName";
        ServiceModelResolver resolver = new ServiceModelResolver(serviceRegistryService);

        Mockito.when(serviceRegistryService.getService(serviceName)).thenReturn(service(100000, serviceName));

        assertThat(resolver.getService(serviceName)).isEqualTo(new Service(serviceName, 100000));
        assertThat(resolver.getService(serviceName)).isEqualTo(new Service(serviceName, 100000));

        Mockito.verify(serviceRegistryService, times(2)).getService(serviceName);
    }

    @Test
    void getStaticService() {
        ServiceModelResolver resolver = new ServiceModelResolver(serviceRegistryService);

        assertThat(resolver.getService(Service.DEFAULT.getServiceName())).isEqualTo(Service.DEFAULT);
        assertThat(resolver.getService(Service.DEFAULT.getServiceUid())).isEqualTo(Service.DEFAULT);

        Mockito.verifyNoInteractions(serviceRegistryService);
    }

    @Test
    void missingServiceThrows() {
        String unRegisteredServiceName = "unRegisteredServiceName";
        ServiceModelResolver resolver = new ServiceModelResolver(serviceRegistryService);

        Mockito.when(serviceRegistryService.getService(unRegisteredServiceName)).thenReturn(null);

        assertThatThrownBy(() -> resolver.getService(unRegisteredServiceName))
                .isInstanceOf(ServiceNotFoundException.class)
                .hasMessageContaining(unRegisteredServiceName);
    }

    @Test
    void missingServiceUidReturnsDefault() {
        int unRegisteredServiceUid = 999999;
        ServiceModelResolver resolver = new ServiceModelResolver(serviceRegistryService);

        Mockito.when(serviceRegistryService.getService(unRegisteredServiceUid)).thenReturn(null);

        assertThat(resolver.getService(unRegisteredServiceUid)).isEqualTo(Service.DEFAULT);
    }

    private Service service(int uid, String name) {
        return new Service(name, uid);
    }
}
