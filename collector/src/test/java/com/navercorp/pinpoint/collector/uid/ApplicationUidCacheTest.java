package com.navercorp.pinpoint.collector.uid;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.collector.uid.service.CachedApplicationUidService;
import com.navercorp.pinpoint.collector.uid.service.CachedApplicationUidServiceImpl;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.uid.service.async.AsyncApplicationUidService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.collector.uid.config.CollectorApplicationUidConfig.APPLICATION_UID_CACHE_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationUidCacheTest.TestConfig.class)
public class ApplicationUidCacheTest {

    @Autowired
    private CachedApplicationUidService cachedService;

    @Autowired
    private ApplicationUidService applicationUidService;

    @Autowired
    private AsyncApplicationUidService asyncApplicationUidService;

    @Autowired
    private CacheManager cacheManager;

    private Cache cache;

    @TestConfiguration
    @EnableCaching
    static class TestConfig {
        @Bean
        public CacheManager applicationUidCache() {
            CaffeineCacheManager cacheManager = new CaffeineCacheManager("applicationUidCache");
            cacheManager.setCaffeine(Caffeine.newBuilder()
                    .initialCapacity(10)
                    .maximumSize(200)
                    .expireAfterWrite(300, TimeUnit.SECONDS));
            cacheManager.setAsyncCacheMode(true);
            cacheManager.setAllowNullValues(false);
            return cacheManager;
        }

        @Bean
        public ApplicationUidService applicationUidService() {
            return mock(ApplicationUidService.class);
        }

        @Bean
        public AsyncApplicationUidService asyncApplicationUidService() {
            return mock(AsyncApplicationUidService.class);
        }

        @Bean
        public CachedApplicationUidService cachedApplicationUidService(ApplicationUidService applicationUidService,
                                                                       AsyncApplicationUidService asyncApplicationUidService,
                                                                       CacheManager cacheManager) {
            return new CachedApplicationUidServiceImpl(applicationUidService, asyncApplicationUidService, cacheManager);
        }
    }

    @BeforeEach
    void clearCache() {
        cache = cacheManager.getCache(APPLICATION_UID_CACHE_NAME);
        cache.clear();
    }

    @Test
    public void cacheTest() {
        String testApplicationName = "cacheTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9991);
        when(applicationUidService.getApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName))
                .thenReturn(testApplicationUid);

        cachedService.getApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName);
        cachedService.getOrCreateApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName);

        verify(applicationUidService, times(1)).getApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName);
        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult)
                .isNotNull()
                .isEqualTo(testApplicationUid);
    }

    @Test
    public void cacheNullValueTest() {
        String testApplicationName = "cacheNullValueTest";
        when(applicationUidService.getApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName))
                .thenReturn(null);

        cachedService.getApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName);

        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult).isNull();
    }

    @Test
    public void asyncCacheTest() {
        String testApplicationName = "asyncCacheTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9992);
        when(asyncApplicationUidService.getOrCreateApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName))
                .thenReturn(CompletableFuture.completedFuture(testApplicationUid));

        CompletableFuture<ApplicationUid> future = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName);
        future.join();

        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult)
                .isNotNull()
                .isEqualTo(testApplicationUid);
    }

    @Test
    public void asyncCacheNullValueTest() {
        String testApplicationName = "asyncCacheNullValueTest";
        when(asyncApplicationUidService.getOrCreateApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<ApplicationUid> future = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName);
        future.join();

        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult).isNull();
    }

    @Test
    public void asyncCacheConcurrentTest() {
        String testApplicationName = "asyncCacheConcurrentTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9993);
        when(asyncApplicationUidService.getOrCreateApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName))
                .thenReturn(CompletableFuture.completedFuture(testApplicationUid));

        CompletableFuture<ApplicationUid> future1 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName);
        CompletableFuture<ApplicationUid> future2 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName);

        CompletableFuture.allOf(future1, future2).join();
        Assertions.assertThat(future1).isEqualTo(future2);
        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT_SERVICE_UID, testApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult)
                .isNotNull()
                .isEqualTo(testApplicationUid);
    }


}
