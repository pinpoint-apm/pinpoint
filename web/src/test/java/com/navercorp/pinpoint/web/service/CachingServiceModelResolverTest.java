package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.service.service.ServiceRegistryService;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import com.navercorp.pinpoint.web.vo.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;

class CachingServiceModelResolverTest {

    private final ServiceModelCacheConfiguration cacheConfiguration = new ServiceModelCacheConfiguration();
    private final CaffeineCacheProperties properties = new CaffeineCacheProperties();
    private final ServiceRegistryService serviceRegistryService = Mockito.mock(ServiceRegistryService.class);

    @BeforeEach
    void setUp() {
        Mockito.reset(serviceRegistryService);
        properties.setMaximumSize(200);
        properties.setExpireAfterWrite(Duration.ofSeconds(-1));
    }

    @Test
    void cacheServiceByName() {
        String serviceName = "serviceName";
        CachingServiceModelResolver resolver = newResolver(Duration.ofMinutes(1));

        Mockito.when(serviceRegistryService.getService(serviceName)).thenReturn(serviceEntity(100001, serviceName));

        assertThat(resolver.getService(serviceName)).isEqualTo(new Service(serviceName, 100001));
        assertThat(resolver.getService(serviceName)).isEqualTo(new Service(serviceName, 100001));

        Mockito.verify(serviceRegistryService, times(1)).getService(serviceName);
    }

    @Test
    void cacheServiceByUid() {
        String serviceName = "serviceName";
        int serviceUid = 100002;
        CachingServiceModelResolver resolver = newResolver(Duration.ofMinutes(1));

        Mockito.when(serviceRegistryService.getService(serviceUid)).thenReturn(serviceEntity(serviceUid, serviceName));

        assertThat(resolver.getService(serviceUid)).isEqualTo(new Service(serviceName, serviceUid));
        assertThat(resolver.getService(serviceUid)).isEqualTo(new Service(serviceName, serviceUid));

        Mockito.verify(serviceRegistryService, times(1)).getService(serviceUid);
    }

    @Test
    void getStaticService() {
        CachingServiceModelResolver resolver = newResolver(Duration.ofMinutes(1));

        assertThat(resolver.getService(Service.DEFAULT.getServiceName())).isEqualTo(Service.DEFAULT);
        assertThat(resolver.getService(Service.DEFAULT.getServiceUid().getUid())).isEqualTo(Service.DEFAULT);
        assertThat(resolver.getService(Service.TEST_SERVICE.getServiceName())).isEqualTo(Service.TEST_SERVICE);
        assertThat(resolver.getService(Service.TEST_SERVICE.getServiceUid().getUid())).isEqualTo(Service.TEST_SERVICE);

        Mockito.verifyNoInteractions(serviceRegistryService);
    }

    @Test
    void cacheMissingService() {
        String unRegisteredServiceName = "unRegisteredServiceName";
        CachingServiceModelResolver resolver = newResolver(Duration.ofMinutes(1));

        Mockito.when(serviceRegistryService.getService(unRegisteredServiceName)).thenReturn(null);

        assertThat(resolver.getService(unRegisteredServiceName)).isEqualTo(Service.DEFAULT);
        assertThat(resolver.getService(unRegisteredServiceName)).isEqualTo(Service.DEFAULT);

        Mockito.verify(serviceRegistryService, times(1)).getService(unRegisteredServiceName);
    }

    @Test
    void cacheMissingServiceExpire() {
        String serviceName = "registeredServiceName";
        String unRegisteredServiceName = "unRegisteredServiceName";
        CachingServiceModelResolver resolver = newResolver(Duration.ZERO);

        Mockito.when(serviceRegistryService.getService(serviceName)).thenReturn(serviceEntity(100004, serviceName));
        Mockito.when(serviceRegistryService.getService(unRegisteredServiceName)).thenReturn(null);

        assertThat(resolver.getService(serviceName)).isEqualTo(new Service(serviceName, 100004));
        assertThat(resolver.getService(serviceName)).isEqualTo(new Service(serviceName, 100004));
        assertThat(resolver.getService(unRegisteredServiceName)).isEqualTo(Service.DEFAULT);

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(resolver.getService(unRegisteredServiceName)).isEqualTo(Service.DEFAULT);
            Mockito.verify(serviceRegistryService, atLeast(2)).getService(unRegisteredServiceName);
        });

        Mockito.verify(serviceRegistryService, times(1)).getService(serviceName);
    }

    @Test
    void refreshLoadsBothCaches() {
        String serviceName = "loadedServiceName";
        int serviceUid = 100005;
        CachingServiceModelResolver resolver = newResolver(Duration.ofMinutes(1));

        Mockito.when(serviceRegistryService.getServiceList(anyInt()))
                .thenReturn(List.of(serviceEntity(serviceUid, serviceName)));

        resolver.refresh();

        assertThat(resolver.getService(serviceName)).isEqualTo(new Service(serviceName, serviceUid));
        assertThat(resolver.getService(serviceUid)).isEqualTo(new Service(serviceName, serviceUid));

        Mockito.verify(serviceRegistryService, times(0)).getService(serviceName);
        Mockito.verify(serviceRegistryService, times(0)).getService(serviceUid);
    }

    @Test
    void warmupLoadsBothCaches() {
        String serviceName = "warmedUpServiceName";
        int serviceUid = 100006;
        CachingServiceModelResolver resolver = newResolver(Duration.ofMinutes(1));

        Mockito.when(serviceRegistryService.getServiceList(anyInt()))
                .thenReturn(List.of(serviceEntity(serviceUid, serviceName)));

        resolver.warmup();

        assertThat(resolver.getService(serviceName)).isEqualTo(new Service(serviceName, serviceUid));
        assertThat(resolver.getService(serviceUid)).isEqualTo(new Service(serviceName, serviceUid));

        Mockito.verify(serviceRegistryService, times(0)).getService(serviceName);
        Mockito.verify(serviceRegistryService, times(0)).getService(serviceUid);
    }

    private CachingServiceModelResolver newResolver(Duration missingExpireAfterWrite) {
        CacheManager cacheManager = cacheConfiguration.webServiceModelCacheManager(properties, missingExpireAfterWrite);
        ServiceModelLoadProperties loadProperties = new ServiceModelLoadProperties();
        return new CachingServiceModelResolver(serviceRegistryService, cacheManager, properties, loadProperties);
    }

    private ServiceEntity serviceEntity(int uid, String name) {
        ServiceEntity service = new ServiceEntity();
        service.setUid(uid);
        service.setName(name);
        return service;
    }
}
