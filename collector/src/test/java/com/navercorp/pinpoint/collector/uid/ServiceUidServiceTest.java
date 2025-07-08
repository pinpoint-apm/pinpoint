package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.config.ServiceUidMysqlCacheConfig;
import com.navercorp.pinpoint.collector.uid.service.ServiceUidService;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.service.component.StaticServiceRegistry;
import com.navercorp.pinpoint.service.service.ServiceInfoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

public class ServiceUidServiceTest {

    StaticServiceRegistry staticServiceRegistry = new StaticServiceRegistry();

    ServiceUidMysqlCacheConfig cacheConfig = new ServiceUidMysqlCacheConfig();

    CaffeineCacheProperties properties = new CaffeineCacheProperties();

    @Test
    public void staticServiceTest() {
        String defaultServiceName = ServiceUid.DEFAULT_SERVICE_UID_NAME;
        ServiceInfoService serviceInfoService = Mockito.mock(ServiceInfoService.class);
        CacheManager cacheManager = cacheConfig.serviceUidCache(properties, Duration.of(1, ChronoUnit.MINUTES));
        ServiceUidService serviceUidService = new ServiceUidService(staticServiceRegistry, serviceInfoService, cacheManager);

        Assertions.assertThat(serviceUidService.getServiceUid(defaultServiceName)).isEqualTo(ServiceUid.DEFAULT);
    }

    @Test
    public void cacheTest() {
        String serviceName = "serviceName";
        ServiceInfoService serviceInfoService = Mockito.mock(ServiceInfoService.class);
        CacheManager cacheManager = cacheConfig.serviceUidCache(properties, Duration.of(1, ChronoUnit.MINUTES));
        ServiceUidService serviceUidService = new ServiceUidService(staticServiceRegistry, serviceInfoService, cacheManager);

        Mockito.when(serviceInfoService.getServiceUid(serviceName)).thenReturn(ServiceUid.of(100001));
        serviceUidService.getServiceUid(serviceName);

        Assertions.assertThat(serviceUidService.getServiceUid(serviceName)).isNotNull();
        Mockito.verify(serviceInfoService, Mockito.times(1)).getServiceUid(serviceName);
    }

    @Test
    public void cacheValueLoaderTest() {
        String serviceName = "cacheValueLoaderTest";
        ServiceInfoService serviceInfoService = Mockito.mock(ServiceInfoService.class);
        CacheManager cacheManager = cacheConfig.serviceUidCache(properties, Duration.of(1, ChronoUnit.MINUTES));
        ServiceUidService serviceUidService = new ServiceUidService(staticServiceRegistry, serviceInfoService, cacheManager);

        Mockito.when(serviceInfoService.getServiceUid(serviceName)).thenAnswer(new AnswersWithDelay(100L, new Returns(ServiceUid.of(100002))));

        CompletableFuture.supplyAsync(() -> serviceUidService.getServiceUid(serviceName));
        CompletableFuture.supplyAsync(() -> serviceUidService.getServiceUid(serviceName));
        CompletableFuture.supplyAsync(() -> serviceUidService.getServiceUid(serviceName));

        Assertions.assertThat(serviceUidService.getServiceUid(serviceName)).isNotNull();
        Mockito.verify(serviceInfoService, Mockito.times(1)).getServiceUid(serviceName);
    }

    @Test
    public void emptyValueCacheTest() {
        String unRegisteredServiceName = "unRegisteredServiceName";
        ServiceInfoService serviceInfoService = Mockito.mock(ServiceInfoService.class);
        CacheManager cacheManager = cacheConfig.serviceUidCache(properties, Duration.of(1, ChronoUnit.MINUTES));
        ServiceUidService serviceUidService = new ServiceUidService(staticServiceRegistry, serviceInfoService, cacheManager);
        Cache cache = cacheManager.getCache(ServiceUidMysqlCacheConfig.SERVICE_UID_CACHE_NAME);

        Mockito.when(serviceInfoService.getServiceUid(unRegisteredServiceName)).thenReturn(null);
        serviceUidService.getServiceUid(unRegisteredServiceName);

        Assertions.assertThat(cache).isNotNull();
        Assertions.assertThat(cache.get(unRegisteredServiceName, ServiceUid.class)).isEqualTo(ServiceUid.NULL);
        Assertions.assertThat(serviceUidService.getServiceUid(unRegisteredServiceName)).isNull();
        Mockito.verify(serviceInfoService, Mockito.times(1)).getServiceUid(unRegisteredServiceName);
    }

    @Test
    public void emptyValueCacheExpireTest() throws InterruptedException {
        String serviceName = "registeredServiceName";
        String unRegisteredServiceName = "unRegisteredServiceName";
        ServiceInfoService serviceInfoService = Mockito.mock(ServiceInfoService.class);
        CacheManager cacheManager = cacheConfig.serviceUidCache(properties, Duration.ZERO);
        ServiceUidService serviceUidService = new ServiceUidService(staticServiceRegistry, serviceInfoService, cacheManager);
        Cache cache = cacheManager.getCache(ServiceUidMysqlCacheConfig.SERVICE_UID_CACHE_NAME);

        Mockito.when(serviceInfoService.getServiceUid(serviceName)).thenReturn(ServiceUid.of(100004));
        Mockito.when(serviceInfoService.getServiceUid(unRegisteredServiceName)).thenReturn(null);
        serviceUidService.getServiceUid(serviceName);
        serviceUidService.getServiceUid(unRegisteredServiceName);

        Thread.sleep(100L);
        Assertions.assertThat(cache).isNotNull();
        Assertions.assertThat(cache.get(serviceName, ServiceUid.class)).isNotNull();
        Assertions.assertThat(cache.get(unRegisteredServiceName, ServiceUid.class)).isNull();
    }
}
