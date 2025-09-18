package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.config.ServiceUidMysqlCacheConfig;
import com.navercorp.pinpoint.collector.uid.service.ServiceUidService;
import com.navercorp.pinpoint.collector.uid.service.ServiceUidServiceImpl;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.cache.CaffeineCacheProperties;
import com.navercorp.pinpoint.service.component.StaticServiceRegistry;
import com.navercorp.pinpoint.service.service.ServiceInfoService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;

public class ServiceUidServiceTest {

    private static final StaticServiceRegistry staticServiceRegistry = new StaticServiceRegistry();
    private final ServiceUidMysqlCacheConfig cacheConfig = new ServiceUidMysqlCacheConfig();
    private final CaffeineCacheProperties properties = new CaffeineCacheProperties();
    private final ServiceInfoService mockServiceInfoService = Mockito.mock(ServiceInfoService.class);

    @BeforeEach
    void setUp() {
        Mockito.reset(mockServiceInfoService);
        properties.setMaximumSize(200);
        properties.setExpireAfterWrite(Duration.of(-1, ChronoUnit.SECONDS));
    }

    @Test
    public void staticServiceTest() {
        String defaultServiceName = ServiceUid.DEFAULT_SERVICE_UID_NAME;
        ServiceUidService serviceUidService = new ServiceUidServiceImpl(staticServiceRegistry, mockServiceInfoService, new NoOpCacheManager());

        Assertions.assertThat(serviceUidService.getServiceUid(defaultServiceName)).isEqualTo(ServiceUid.DEFAULT);
    }

    @Test
    public void cacheTest() {
        String serviceName = "serviceName";
        CacheManager cacheManager = cacheConfig.serviceUidCache(properties, Duration.of(1, ChronoUnit.MINUTES));
        Cache cache = getServiceUidCache(cacheManager);
        ServiceUidService serviceUidService = new ServiceUidServiceImpl(staticServiceRegistry, mockServiceInfoService, cacheManager);

        Mockito.when(mockServiceInfoService.getServiceUid(serviceName)).thenReturn(ServiceUid.of(100001));
        ServiceUid first = serviceUidService.getServiceUid(serviceName);
        ServiceUid second = serviceUidService.getServiceUid(serviceName);

        Assertions.assertThat(first).isEqualTo(second);
        assertCachedResult(cache, serviceName);
    }

    @Test
    public void cacheValueLoaderTest() {
        String serviceName = "cacheValueLoaderTest";
        CacheManager cacheManager = cacheConfig.serviceUidCache(properties, Duration.of(1, ChronoUnit.MINUTES));
        Cache cache = getServiceUidCache(cacheManager);
        ServiceUidService serviceUidService = new ServiceUidServiceImpl(staticServiceRegistry, mockServiceInfoService, cacheManager);

        Mockito.when(mockServiceInfoService.getServiceUid(serviceName)).thenAnswer(new AnswersWithDelay(100L, new Returns(ServiceUid.of(100002))));
        CompletableFuture<ServiceUid> future1 = CompletableFuture.supplyAsync(() -> serviceUidService.getServiceUid(serviceName));
        CompletableFuture<ServiceUid> future2 = CompletableFuture.supplyAsync(() -> serviceUidService.getServiceUid(serviceName));
        CompletableFuture<ServiceUid> future3 = CompletableFuture.supplyAsync(() -> serviceUidService.getServiceUid(serviceName));
        CompletableFuture.allOf(future1, future2, future3).join();

        assertCachedResult(cache, serviceName);
    }

    @Test
    public void cacheNullValueHandleTest() {
        String unRegisteredServiceName = "unRegisteredServiceName";
        CacheManager cacheManager = cacheConfig.serviceUidCache(properties, Duration.of(1, ChronoUnit.MINUTES));
        Cache cache = getServiceUidCache(cacheManager);
        ServiceUidService serviceUidService = new ServiceUidServiceImpl(staticServiceRegistry, mockServiceInfoService, cacheManager);

        Mockito.when(mockServiceInfoService.getServiceUid(unRegisteredServiceName)).thenReturn(null);
        serviceUidService.getServiceUid(unRegisteredServiceName);
        serviceUidService.getServiceUid(unRegisteredServiceName);

        assertCachedResult(cache, unRegisteredServiceName);
        Assertions.assertThat(cache.get(unRegisteredServiceName, ServiceUid.class)).isNull();
    }

    @Test
    public void cacheNullValueExpireTest() {
        String serviceName = "registeredServiceName";
        String unRegisteredServiceName = "unRegisteredServiceName";
        CacheManager cacheManager = cacheConfig.serviceUidCache(properties, Duration.ZERO);
        ServiceUidService serviceUidService = new ServiceUidServiceImpl(staticServiceRegistry, mockServiceInfoService, cacheManager);

        Mockito.when(mockServiceInfoService.getServiceUid(serviceName)).thenReturn(ServiceUid.of(100004));
        Mockito.when(mockServiceInfoService.getServiceUid(unRegisteredServiceName)).thenReturn(null);
        serviceUidService.getServiceUid(serviceName);
        serviceUidService.getServiceUid(unRegisteredServiceName);
        serviceUidService.getServiceUid(unRegisteredServiceName);

        Cache cache = getServiceUidCache(cacheManager);
        assertCachedResult(cache, serviceName);
        Assertions.assertThat(cache.get(unRegisteredServiceName)).isNull();
        Assertions.assertThat(cache.get(unRegisteredServiceName, ServiceUid.class)).isNull();
        Mockito.verify(mockServiceInfoService, atLeast(2)).getServiceUid(unRegisteredServiceName);
    }

    private Cache getServiceUidCache(CacheManager cacheManager) {
        return Objects.requireNonNull(cacheManager.getCache(ServiceUidMysqlCacheConfig.SERVICE_UID_CACHE_NAME), "cache");
    }

    private void assertCachedResult(Cache cache, String cachedServiceName) {
        Assertions.assertThat(cache.get(cachedServiceName)).isNotNull();
        Mockito.verify(mockServiceInfoService, atMost(1)).getServiceUid(cachedServiceName);
    }
}
