package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.config.ApplicationUidCacheConfig;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidCachedServiceImpl;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.navercorp.pinpoint.collector.uid.config.ApplicationUidCacheConfig.APPLICATION_UID_CACHE_NAME;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "pinpoint.modules.uid.enabled=true",
        "collector.application.uid.cache.maximumSize=200",
        "collector.application.uid.cache.expireAfterWrite=300s"
})
@Import({
        ApplicationUidCacheConfig.class,
        ApplicationUidCachedServiceImpl.class
})
public class ApplicationUidCacheTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @MockBean
    private BaseApplicationUidService mockBaseApplicationUidService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ApplicationUidService cachedService;

    private Cache cache;

    @BeforeEach
    void setup() {
        Mockito.reset(mockBaseApplicationUidService);
        cache = Objects.requireNonNull(cacheManager.getCache(APPLICATION_UID_CACHE_NAME), "cache");
        cache.clear();
    }

    @Test
    public void getApplicationUidCacheTest() {
        String testApplicationName = "getApplicationUidCacheTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9991);
        when(mockBaseApplicationUidService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                .thenReturn(testApplicationUid);

        cachedService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        cachedService.getOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);

        assertCachedResult(ServiceUid.DEFAULT, testApplicationName, testApplicationUid);
    }

    @Test
    public void asyncGetOrCreateApplicationUidCacheTest() {
        String testApplicationName = "asyncGetOrCreateApplicationUidCacheTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9992);
        when(mockBaseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                .thenReturn(CompletableFuture.completedFuture(testApplicationUid));

        CompletableFuture<ApplicationUid> future = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        future.join();
        cachedService.getOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        cachedService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName);

        assertCachedResult(ServiceUid.DEFAULT, testApplicationName, testApplicationUid);
    }

    @Test
    public void cacheNullValueIgnoreTest() {
        String testApplicationName = "cacheNullValueTest";
        when(mockBaseApplicationUidService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                .thenReturn(null);

        cachedService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName);

        Cache.ValueWrapper valueWrapper = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT, testApplicationName));
        Assertions.assertThat(valueWrapper).isNull();
    }

    @Test
    public void asyncCacheTest() {
        String testApplicationName = "asyncCacheTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9992);
        when(mockBaseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                .thenReturn(CompletableFuture.completedFuture(testApplicationUid));

        CompletableFuture<ApplicationUid> future = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        future.join();

        assertCachedResult(ServiceUid.DEFAULT, testApplicationName, testApplicationUid);
    }

    @Test
    public void asyncCacheNullValueTest() {
        String testApplicationName = "asyncCacheNullValueTest";
        when(mockBaseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<ApplicationUid> future = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
        future.join();

        assertUncachedResult(ServiceUid.DEFAULT, testApplicationName);
    }

    private ApplicationUid getCachedResult(ServiceUid serviceUid, String applicationName) {
        return cache.get(SimpleKeyGenerator.generateKey(serviceUid, applicationName), ApplicationUid.class);
    }

    @Test
    public void asyncCacheExceptionTest() {
        String testApplicationName = "asyncCacheExceptionTest";
        String testExceptionMessage = "test exception";

        ExecutorService executor = Executors.newFixedThreadPool(2, PinpointThreadFactory.createThreadFactory("async-cache-test"));
        try {
            when(mockBaseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                    .thenAnswer(invocation -> CompletableFuture.supplyAsync(() -> {
                        delay(100);
                        throw new RuntimeException(testExceptionMessage);
                    }, executor));

            CompletableFuture<ApplicationUid> future1 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
            Assertions.assertThatThrownBy(future1::join).isInstanceOf(RuntimeException.class);
            delay(100);

            CompletableFuture<ApplicationUid> future2 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
            Assertions.assertThatThrownBy(future2::join)
                    .isInstanceOf(CompletionException.class)
                    .cause()
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(testExceptionMessage);

            Assertions.assertThat(future2).isNotEqualTo(future1);
            assertUncachedResult(ServiceUid.DEFAULT, testApplicationName);
        } finally {
            executor.shutdown();
        }
    }

    @Test
    public void asyncCacheConcurrentTest() {
        String testApplicationName = "asyncCacheConcurrentTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9993);

        ExecutorService executor = Executors.newFixedThreadPool(2, PinpointThreadFactory.createThreadFactory("async-cache-test"));
        try {
            when(mockBaseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName))
                    .thenAnswer(invocation -> CompletableFuture.supplyAsync(() -> {
                        delay(100);
                        return testApplicationUid;
                    }, executor));

            CompletableFuture<ApplicationUid> future1 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
            CompletableFuture<ApplicationUid> future2 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
            future1.join();
            future2.join();

            CompletableFuture<ApplicationUid> future3 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName);
            future3.join();

            Assertions.assertThat(future1).isEqualTo(future2);
            Assertions.assertThat(future1.join()).isEqualTo(future3.join());
            assertCachedResult(ServiceUid.DEFAULT, testApplicationName, testApplicationUid);
        } finally {
            executor.shutdown();
        }
    }

    private void assertUncachedResult(ServiceUid serviceUid, String cachedApplicationName) {
        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(serviceUid, cachedApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult).isNull();
    }

    private void assertCachedResult(ServiceUid serviceUid, String cachedApplicationName, ApplicationUid expected) {
        ApplicationUid cachedResult = cache.get(SimpleKeyGenerator.generateKey(serviceUid, cachedApplicationName), ApplicationUid.class);
        Assertions.assertThat(cachedResult)
                .isNotNull()
                .isEqualTo(expected);
        Mockito.verify(mockBaseApplicationUidService, atMost(1)).getApplicationUid(serviceUid, cachedApplicationName);
        Mockito.verify(mockBaseApplicationUidService, atMost(1)).asyncGetOrCreateApplicationUid(serviceUid, cachedApplicationName);
    }

    private void delay(long millis) {
        logger.info("Thread : {}", Thread.currentThread().getName());
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
