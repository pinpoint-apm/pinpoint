package com.navercorp.pinpoint.collector.uid;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidCachedServiceImpl;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
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

import static com.navercorp.pinpoint.collector.uid.config.ApplicationUidCacheConfig.APPLICATION_UID_CACHE_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApplicationUidCacheTest.TestConfig.class)
public class ApplicationUidCacheTest {

    @Autowired
    private ApplicationUidService cachedService;

    @Autowired
    private BaseApplicationUidService baseApplicationUidService;

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
        public BaseApplicationUidService applicationService() {
            return mock(BaseApplicationUidService.class);
        }

        @Bean
        public ApplicationUidService ApplicationUidService(BaseApplicationUidService baseApplicationUidService,
                                                           CacheManager cacheManager) {
            return new ApplicationUidCachedServiceImpl(baseApplicationUidService, cacheManager);
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
        when(baseApplicationUidService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                .thenReturn(testApplicationUid);

        cachedService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        cachedService.getOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);

        verify(baseApplicationUidService, times(1)).getApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT, testApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult)
                .isNotNull()
                .isEqualTo(testApplicationUid);
    }

    @Test
    public void cacheNullValueTest() {
        String testApplicationName = "cacheNullValueTest";
        when(baseApplicationUidService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                .thenReturn(null);

        cachedService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName);

        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT, testApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult).isNull();
    }

    @Test
    public void asyncCacheTest() {
        String testApplicationName = "asyncCacheTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9992);
        when(baseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                .thenReturn(CompletableFuture.completedFuture(testApplicationUid));

        CompletableFuture<ApplicationUid> future = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        future.join();

        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT, testApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult)
                .isNotNull()
                .isEqualTo(testApplicationUid);
    }

    @Test
    public void asyncCacheNullValueTest() {
        String testApplicationName = "asyncCacheNullValueTest";
        when(baseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<ApplicationUid> future = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        future.join();

        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT, testApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult).isNull();
    }

    @Test
    public void asyncCacheConcurrentTest() {
        String testApplicationName = "asyncCacheConcurrentTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9993);
        when(baseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                .thenReturn(CompletableFuture.completedFuture(testApplicationUid));

        CompletableFuture<ApplicationUid> future1 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        CompletableFuture<ApplicationUid> future2 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);

        CompletableFuture.allOf(future1, future2).join();
        Assertions.assertThat(future1).isEqualTo(future2);
        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT, testApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult)
                .isNotNull()
                .isEqualTo(testApplicationUid);
    }


}
