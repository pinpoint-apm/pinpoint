package com.navercorp.pinpoint.uid;

import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.RandomApplicationUidGenerator;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.uid.dao.ApplicationUidAttrDao;
import com.navercorp.pinpoint.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.uid.dao.ConcurrentMapApplicationUidAttrDao;
import com.navercorp.pinpoint.uid.dao.ConcurrentMapApplicationUidDao;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

@Disabled
public class AsyncBaseApplicationUidServiceTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServiceUid testServiceUid = ServiceUid.DEFAULT;
    private final int testServiceTypeCode = ServiceType.TEST.getCode();

    private static ExecutorService executorService;

    private static BaseApplicationUidService baseApplicationUidService;

    @BeforeAll
    public static void setUp() {
        ThreadFactory threadFactory = new PinpointThreadFactory("test-dao", true);
        executorService = ExecutorFactory.newFixedThreadPool(2, 32, threadFactory);

        ApplicationUidDao applicationUidDao = new ConcurrentMapApplicationUidDao(executorService, 50);
        ApplicationUidAttrDao applicationUidAttrDao = new ConcurrentMapApplicationUidAttrDao(executorService, 50);

        baseApplicationUidService = new BaseApplicationUidServiceImpl(applicationUidDao, applicationUidAttrDao, new RandomApplicationUidGenerator());
    }

    @AfterAll
    public static void tearDown() {
        executorService.shutdown();
    }

    @Test
    public void getOrCreateApplicationIdTest() {
        String testApplicationName = "test1";

        ApplicationUid before = baseApplicationUidService.getApplicationUid(testServiceUid, testApplicationName, testServiceTypeCode);
        ApplicationUid newApplicationUid = baseApplicationUidService.asyncGetOrCreateApplicationUid(testServiceUid, testApplicationName, testServiceTypeCode).join();
        ApplicationUid after = baseApplicationUidService.getApplicationUid(testServiceUid, testApplicationName, testServiceTypeCode);

        Assertions.assertThat(before).isNull();
        Assertions.assertThat(newApplicationUid).isNotNull();
        Assertions.assertThat(after).isEqualTo(newApplicationUid);
    }

    @Test
    public void getOrCreateApplicationIdConcurrentTest() {
        String testApplicationName = "test2";
        int numberOfRequest = 5;
        long testStartTime = System.currentTimeMillis();

        List<CompletableFuture<ApplicationUid>> futures = new ArrayList<>(numberOfRequest);
        for (int i = 0; i < numberOfRequest; i++) {
            futures.add(baseApplicationUidService.asyncGetOrCreateApplicationUid(testServiceUid, testApplicationName, testServiceTypeCode));
        }
        logger.info("1. async invocation. elapsed:{}, numberOfRequest:{}", System.currentTimeMillis() - testStartTime, numberOfRequest);

        Set<ApplicationUid> result = new HashSet<>();
        for (CompletableFuture<ApplicationUid> future : futures) {
            result.add(future.join());
        }
        logger.info("2. after completion. elapsed:{}, numberOfRequest:{}", System.currentTimeMillis() - testStartTime, numberOfRequest);

        Assertions.assertThat(futures).hasSize(numberOfRequest);
        Assertions.assertThat(result).hasSize(1);
    }

    @Test
    public void getOrCreateApplicationIdRollbackTest() {
        String testApplicationName = "test2";
        int numberOfRequest = 5;
        long testStartTime = System.currentTimeMillis();

        List<CompletableFuture<ApplicationUid>> futures = new ArrayList<>(numberOfRequest);
        for (int i = 0; i < numberOfRequest; i++) {
            futures.add(baseApplicationUidService.asyncGetOrCreateApplicationUid(testServiceUid, testApplicationName, testServiceTypeCode));
        }
        logger.info("1. async invocation. elapsed:{}, numberOfRequest:{}", System.currentTimeMillis() - testStartTime, numberOfRequest);

        Set<ApplicationUid> result = new HashSet<>();
        for (CompletableFuture<ApplicationUid> future : futures) {
            result.add(future.join());
        }
        logger.info("2. after completion. elapsed:{}, numberOfRequest:{}", System.currentTimeMillis() - testStartTime, numberOfRequest);

        Assertions.assertThat(result).hasSize(1);
    }
}
