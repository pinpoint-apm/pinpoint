package com.navercorp.pinpoint.collector.uid;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.navercorp.pinpoint.collector.uid.config.ApplicationUidConfig;
import com.navercorp.pinpoint.collector.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.collector.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.collector.uid.dao.ConcurrentMapApplicationNameDao;
import com.navercorp.pinpoint.collector.uid.dao.ConcurrentMapApplicationUidDao;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidServiceImpl;
import com.navercorp.pinpoint.collector.uid.service.async.AsyncApplicationUidService;
import com.navercorp.pinpoint.collector.uid.service.async.AsyncApplicationUidServiceImpl;
import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.RandomApplicationUidGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.support.NoOpCacheManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class AsyncApplicationUidServiceTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServiceUid testServiceUid = ServiceUid.DEFAULT;

    private static ExecutorService executorService;
    private static ApplicationUidDao applicationUidDao;
    private static ApplicationNameDao applicationNameDao;

    private static ApplicationUidService applicationUidService;

    private static CaffeineCacheManager caffeineCacheManager;
    private static Cache cache;

    @BeforeAll
    public static void setUp() {
        ThreadFactory threadFactory = new PinpointThreadFactory("test-dao", true);
        executorService = ExecutorFactory.newFixedThreadPool(2, 32, threadFactory);

        applicationUidDao = new ConcurrentMapApplicationUidDao(executorService, 50);
        applicationNameDao = new ConcurrentMapApplicationNameDao(executorService, 50);

        applicationUidService = new ApplicationUidServiceImpl(applicationUidDao, applicationNameDao,
                new RandomApplicationUidGenerator());

        caffeineCacheManager = new CaffeineCacheManager(ApplicationUidConfig.APPLICATION_UID_CACHE_NAME);
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(10)
                .maximumSize(200)
                .expireAfterWrite(300, TimeUnit.SECONDS)
        );
        caffeineCacheManager.setAsyncCacheMode(true);
        caffeineCacheManager.setAllowNullValues(false);

        cache = caffeineCacheManager.getCache("applicationUidCache");
    }

    @AfterAll
    public static void tearDown() {
        executorService.shutdown();
    }

    @Test
    public void getOrCreateApplicationIdTest() {
        AsyncApplicationUidService asyncApplicationUidService = new AsyncApplicationUidServiceImpl(applicationUidDao, applicationNameDao,
                new RandomApplicationUidGenerator(), caffeineCacheManager);
        String testApplicationName = "test1";

        ApplicationUid before = applicationUidService.getApplicationUid(testServiceUid, testApplicationName);
        ApplicationUid newApplicationUid = asyncApplicationUidService.getOrCreateApplicationUid(testServiceUid, testApplicationName).join();
        ApplicationUid after = applicationUidService.getApplicationUid(testServiceUid, testApplicationName);

        Assertions.assertThat(before).isNull();
        Assertions.assertThat(newApplicationUid).isNotNull();
        Assertions.assertThat(after).isEqualTo(newApplicationUid);
    }

    @Test
    public void getOrCreateApplicationIdConcurrentTest() {
        AsyncApplicationUidService asyncApplicationUidService = new AsyncApplicationUidServiceImpl(applicationUidDao, applicationNameDao,
                new RandomApplicationUidGenerator(), caffeineCacheManager);
        String testApplicationName = "test2";
        int numberOfRequest = 5;
        long testStartTime = System.currentTimeMillis();

        List<CompletableFuture<ApplicationUid>> futures = new ArrayList<>(numberOfRequest);
        for (int i = 0; i < numberOfRequest; i++) {
            futures.add(asyncApplicationUidService.getOrCreateApplicationUid(testServiceUid, testApplicationName));
        }
        logger.info("1. async invocation. elapsed:{}, numberOfRequest:{}", System.currentTimeMillis() - testStartTime, numberOfRequest);

        Set<ApplicationUid> result = new HashSet<>();
        for (CompletableFuture<ApplicationUid> future : futures) {
            result.add(future.join());
        }
        logger.info("2. after completion. elapsed:{}, numberOfRequest:{}", System.currentTimeMillis() - testStartTime, numberOfRequest);

        Assertions.assertThat(futures).containsOnly(futures.get(0));
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    public void getOrCreateApplicationIdRollbackTest() {
        AsyncApplicationUidService asyncApplicationUidService = new AsyncApplicationUidServiceImpl(applicationUidDao, applicationNameDao,
                new RandomApplicationUidGenerator(), new NoOpCacheManager());
        String testApplicationName = "test2";
        int numberOfRequest = 5;
        long testStartTime = System.currentTimeMillis();

        List<CompletableFuture<ApplicationUid>> futures = new ArrayList<>(numberOfRequest);
        for (int i = 0; i < numberOfRequest; i++) {
            futures.add(asyncApplicationUidService.getOrCreateApplicationUid(testServiceUid, testApplicationName));
        }
        logger.info("1. async invocation. elapsed:{}, numberOfRequest:{}", System.currentTimeMillis() - testStartTime, numberOfRequest);

        Set<ApplicationUid> result = new HashSet<>();
        for (CompletableFuture<ApplicationUid> future : futures) {
            result.add(future.join());
        }
        logger.info("2. after completion. elapsed:{}, numberOfRequest:{}", System.currentTimeMillis() - testStartTime, numberOfRequest);

        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    public void getOrCreateApplicationIdCacheResultTest() {
        AsyncApplicationUidService asyncApplicationUidService = new AsyncApplicationUidServiceImpl(applicationUidDao, applicationNameDao,
                new RandomApplicationUidGenerator(), caffeineCacheManager);

        String testApplicationName = "test4";
        int numberOfRequest = 3;

        List<CompletableFuture<ApplicationUid>> futures = new ArrayList<>(numberOfRequest);
        for (int i = 0; i < numberOfRequest; i++) {
            futures.add(asyncApplicationUidService.getOrCreateApplicationUid(testServiceUid, testApplicationName));
        }
        Set<ApplicationUid> result = new HashSet<>();
        for (CompletableFuture<ApplicationUid> future : futures) {
            result.add(future.join());
        }

        logger.info("Cached with applicationName:{}", testApplicationName);
        ApplicationUid cachedResult = cache.get(new SimpleKey(testServiceUid, testApplicationName), ApplicationUid.class);
        ApplicationUid after = asyncApplicationUidService.getApplicationUid(testServiceUid, testApplicationName).join();

        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(cachedResult).isNotNull();
        Assertions.assertThat(result).contains(cachedResult);
        Assertions.assertThat(after).isEqualTo(cachedResult);
    }

}
