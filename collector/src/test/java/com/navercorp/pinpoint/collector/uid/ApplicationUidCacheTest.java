/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.config.ApplicationUidCacheConfig;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidCachedServiceImpl;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.navercorp.pinpoint.collector.uid.config.ApplicationUidCacheConfig.APPLICATION_UID_CACHE_NAME;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.when;

@Deprecated
@Disabled
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
    private final int testServiceTypeCode = ServiceType.TEST.getCode();

    @MockitoBean
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
        when(mockBaseApplicationUidService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode))
                .thenReturn(testApplicationUid);

        cachedService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
        cachedService.getOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
        cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);

        assertCachedResult(ServiceUid.DEFAULT, testApplicationName, testApplicationUid);
    }

    @Test
    public void asyncGetOrCreateApplicationUidCacheTest() {
        String testApplicationName = "asyncGetOrCreateApplicationUidCacheTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9992);
        when(mockBaseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode))
                .thenReturn(CompletableFuture.completedFuture(testApplicationUid));

        CompletableFuture<ApplicationUid> future = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
        future.join();
        cachedService.getOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
        cachedService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);

        assertCachedResult(ServiceUid.DEFAULT, testApplicationName, testApplicationUid);
    }

    @Test
    public void cacheNullValueIgnoreTest() {
        String testApplicationName = "cacheNullValueTest";
        when(mockBaseApplicationUidService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode))
                .thenReturn(null);

        cachedService.getApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);

        Cache.ValueWrapper valueWrapper = cache.get(SimpleKeyGenerator.generateKey(ServiceUid.DEFAULT, testApplicationName));
        Assertions.assertThat(valueWrapper).isNull();
    }

    @Test
    public void asyncCacheTest() {
        String testApplicationName = "asyncCacheTest";
        ApplicationUid testApplicationUid = ApplicationUid.of(9992);
        when(mockBaseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode))
                .thenReturn(CompletableFuture.completedFuture(testApplicationUid));

        CompletableFuture<ApplicationUid> future = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
        future.join();

        assertCachedResult(ServiceUid.DEFAULT, testApplicationName, testApplicationUid);
    }

    @Test
    public void asyncCacheNullValueTest() {
        String testApplicationName = "asyncCacheNullValueTest";
        when(mockBaseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<ApplicationUid> future = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
        future.join();

        assertUncachedResult(ServiceUid.DEFAULT, testApplicationName);
    }

    @Test
    public void asyncCacheExceptionTest() {
        String testApplicationName = "asyncCacheExceptionTest";
        String testExceptionMessage = "test exception";

        ExecutorService executor = Executors.newFixedThreadPool(2, PinpointThreadFactory.createThreadFactory("async-cache-test"));
        try {
            when(mockBaseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode))
                    .thenAnswer(invocation -> CompletableFuture.supplyAsync(() -> {
                        delay(100);
                        throw new RuntimeException(testExceptionMessage);
                    }, executor));

            CompletableFuture<ApplicationUid> future1 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
            Assertions.assertThatThrownBy(future1::join).isInstanceOf(RuntimeException.class);
            delay(100);

            CompletableFuture<ApplicationUid> future2 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
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
            when(mockBaseApplicationUidService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode))
                    .thenAnswer(invocation -> CompletableFuture.supplyAsync(() -> {
                        delay(100);
                        return testApplicationUid;
                    }, executor));

            CompletableFuture<ApplicationUid> future1 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
            CompletableFuture<ApplicationUid> future2 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
            future1.join();
            future2.join();

            CompletableFuture<ApplicationUid> future3 = cachedService.asyncGetOrCreateApplicationUid(ServiceUid.DEFAULT, testApplicationName, testServiceTypeCode);
            future3.join();

            Assertions.assertThat(future1).isEqualTo(future2);
            Assertions.assertThat(future1.join()).isEqualTo(future3.join());
            assertCachedResult(ServiceUid.DEFAULT, testApplicationName, testApplicationUid);
        } finally {
            executor.shutdown();
        }
    }

    private Object generateCacheKey(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        return SimpleKeyGenerator.generateKey(serviceUid, applicationName, serviceTypeCode);
    }

    private void assertUncachedResult(ServiceUid serviceUid, String cachedApplicationName) {
        ApplicationUid cachedResult = cache.get(generateCacheKey(serviceUid, cachedApplicationName, testServiceTypeCode), ApplicationUid.class);
        Assertions.assertThat(cachedResult).isNull();
    }

    private void assertCachedResult(ServiceUid serviceUid, String cachedApplicationName, ApplicationUid expected) {
        ApplicationUid cachedResult = cache.get(generateCacheKey(serviceUid, cachedApplicationName, testServiceTypeCode), ApplicationUid.class);
        Assertions.assertThat(cachedResult)
                .isNotNull()
                .isEqualTo(expected);
        Mockito.verify(mockBaseApplicationUidService, atMost(1)).getApplicationUid(serviceUid, cachedApplicationName, testServiceTypeCode);
        Mockito.verify(mockBaseApplicationUidService, atMost(1)).asyncGetOrCreateApplicationUid(serviceUid, cachedApplicationName, testServiceTypeCode);
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
