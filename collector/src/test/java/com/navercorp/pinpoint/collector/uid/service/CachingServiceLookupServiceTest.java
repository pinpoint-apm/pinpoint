package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.collector.uid.config.ServiceLookupCacheConfiguration;
import com.navercorp.pinpoint.collector.uid.config.ServiceLookupLoadProperties;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.service.dao.ServiceRegistryDao;
import com.navercorp.pinpoint.service.vo.ServiceEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;

class CachingServiceLookupServiceTest {

    private final ServiceLookupCacheConfiguration cacheConfiguration = new ServiceLookupCacheConfiguration();
    private final CaffeineCacheProperties properties = new CaffeineCacheProperties();
    private final ServiceRegistryDao serviceRegistryDao = Mockito.mock(ServiceRegistryDao.class);
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        Mockito.reset(serviceRegistryDao);
        properties.setMaximumSize(200);
        properties.setExpireAfterWrite(Duration.ofSeconds(-1));
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void cacheServiceUid() {
        String serviceName = "serviceName";
        ServiceLookupService service = newService(Duration.ofMinutes(1));

        Mockito.when(serviceRegistryDao.selectService(serviceName)).thenReturn(serviceEntity(100001, serviceName));

        assertThat(service.getServiceUid(serviceName).join()).isEqualTo(ServiceUid.of(100001));
        assertThat(service.getServiceUid(serviceName).join()).isEqualTo(ServiceUid.of(100001));

        Mockito.verify(serviceRegistryDao, times(1)).selectService(serviceName);
    }

    @Test
    void cacheValueLoaderTest() throws InterruptedException {
        String serviceName = "cacheValueLoaderTest";
        ServiceLookupService service = newService(Duration.ofMinutes(1));
        CountDownLatch loaderStarted = new CountDownLatch(1);
        CountDownLatch completeLoader = new CountDownLatch(1);

        Mockito.when(serviceRegistryDao.selectService(serviceName)).thenAnswer(invocation -> {
            loaderStarted.countDown();
            assertThat(completeLoader.await(1, TimeUnit.SECONDS)).isTrue();
            return serviceEntity(100002, serviceName);
        });

        CompletableFuture<ServiceUid> future1 = service.getServiceUid(serviceName);
        assertThat(loaderStarted.await(1, TimeUnit.SECONDS)).isTrue();
        CompletableFuture<ServiceUid> future2 = service.getServiceUid(serviceName);
        CompletableFuture<ServiceUid> future3 = service.getServiceUid(serviceName);

        completeLoader.countDown();
        assertThat(future1.join()).isEqualTo(ServiceUid.of(100002));
        assertThat(future2.join()).isEqualTo(ServiceUid.of(100002));
        assertThat(future3.join()).isEqualTo(ServiceUid.of(100002));
        Mockito.verify(serviceRegistryDao, times(1)).selectService(serviceName);
    }

    @Test
    void cacheMissingServiceUid() {
        String unRegisteredServiceName = "unRegisteredServiceName";
        ServiceLookupService service = newService(Duration.ofMinutes(1));

        Mockito.when(serviceRegistryDao.selectService(unRegisteredServiceName)).thenReturn(null);

        assertThat(service.getServiceUid(unRegisteredServiceName).join()).isNull();
        assertThat(service.getServiceUid(unRegisteredServiceName).join()).isNull();

        Mockito.verify(serviceRegistryDao, times(1)).selectService(unRegisteredServiceName);
    }

    @Test
    void cacheMissingServiceUidExpireTest() {
        String serviceName = "registeredServiceName";
        String unRegisteredServiceName = "unRegisteredServiceName";
        ServiceLookupService service = newService(Duration.ZERO);

        Mockito.when(serviceRegistryDao.selectService(serviceName)).thenReturn(serviceEntity(100004, serviceName));
        Mockito.when(serviceRegistryDao.selectService(unRegisteredServiceName)).thenReturn(null);

        assertThat(service.getServiceUid(serviceName).join()).isEqualTo(ServiceUid.of(100004));
        assertThat(service.getServiceUid(serviceName).join()).isEqualTo(ServiceUid.of(100004));
        assertThat(service.getServiceUid(unRegisteredServiceName).join()).isNull();

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(service.getServiceUid(unRegisteredServiceName).join()).isNull();
            Mockito.verify(serviceRegistryDao, atLeast(2)).selectService(unRegisteredServiceName);
        });

        Mockito.verify(serviceRegistryDao, times(1)).selectService(serviceName);
    }

    private ServiceLookupService newService(Duration missingExpireAfterWrite) {
        CacheManager cacheManager = cacheConfiguration.collectorServiceLookupCacheManager(properties, missingExpireAfterWrite);
        ServiceLookupLoadProperties loadProperties = new ServiceLookupLoadProperties();
        loadProperties.setWarmupEnabled(false);
        return new CachingServiceLookupService(properties, cacheManager, serviceRegistryDao, loadProperties, executor);
    }

    private ServiceEntity serviceEntity(int uid, String name) {
        ServiceEntity service = new ServiceEntity();
        service.setUid(uid);
        service.setName(name);
        return service;
    }
}
