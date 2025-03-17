package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.collector.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.collector.uid.dao.ConcurrentMapApplicationNameDao;
import com.navercorp.pinpoint.collector.uid.dao.ConcurrentMapApplicationUidDao;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public class AsyncApplicationUidServiceTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ServiceUid testServiceUid = ServiceUid.DEFAULT_SERVICE_UID;

    private static ExecutorService executorService;
    private static AsyncApplicationUidService asyncApplicationUidService;

    @BeforeAll
    public static void setUp() {
        ThreadFactory threadFactory = new PinpointThreadFactory("test-dao", true);
        executorService = ExecutorFactory.newFixedThreadPool(2, 32, threadFactory);

        ApplicationUidDao testApplicationUidDao = new ConcurrentMapApplicationUidDao(executorService, 50);
        ApplicationNameDao testApplicationNameDao = new ConcurrentMapApplicationNameDao(executorService, 50);
        asyncApplicationUidService = new AsyncApplicationUidServiceImpl(testApplicationUidDao, testApplicationNameDao, new RandomApplicationUidGenerator());
    }

    @AfterAll
    public static void tearDown() {
        executorService.shutdown();
    }


    @Test
    public void getOrCreateApplicationIdTest() {
        String testApplicationName = "test1";

        ApplicationUid before = asyncApplicationUidService.getApplicationId(testServiceUid, testApplicationName).join();
        ApplicationUid newApplicationUid = asyncApplicationUidService.getOrCreateApplicationId(testServiceUid, testApplicationName).join();
        ApplicationUid applicationUid = asyncApplicationUidService.getOrCreateApplicationId(testServiceUid, testApplicationName).join();

        Assertions.assertThat(before).isNull();
        Assertions.assertThat(newApplicationUid).isNotNull();
        Assertions.assertThat(applicationUid).isEqualTo(newApplicationUid);
    }

    @Test
    public void getOrCreateApplicationIdConcurrentTest() {
        String testApplicationName = "test2";
        int numberOfRequest = 5;
        long testStartTime = System.currentTimeMillis();

        List<CompletableFuture<ApplicationUid>> futures = new ArrayList<>(numberOfRequest);
        for (int i = 0; i < numberOfRequest; i++) {
            futures.add(asyncApplicationUidService.getOrCreateApplicationId(testServiceUid, testApplicationName));
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
