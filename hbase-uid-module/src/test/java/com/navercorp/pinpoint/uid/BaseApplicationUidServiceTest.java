package com.navercorp.pinpoint.uid;

import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.RandomApplicationUidGenerator;
import com.navercorp.pinpoint.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.uid.dao.ConcurrentMapApplicationNameDao;
import com.navercorp.pinpoint.uid.dao.ConcurrentMapApplicationUidDao;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class BaseApplicationUidServiceTest {

    private final ServiceUid testServiceUid = ServiceUid.DEFAULT;

    private static ExecutorService executorService;

    private static BaseApplicationUidService applicationIdService;

    @BeforeAll
    public static void setUp() {
        ThreadFactory threadFactory = new PinpointThreadFactory("notUsed", true);
        executorService = ExecutorFactory.newFixedThreadPool(2, 2, threadFactory);

        ApplicationUidDao testApplicationUidDao = new ConcurrentMapApplicationUidDao(executorService);
        ApplicationNameDao testApplicationNameDao = new ConcurrentMapApplicationNameDao(executorService);
        applicationIdService = new BaseApplicationUidServiceImpl(testApplicationUidDao, testApplicationNameDao, new RandomApplicationUidGenerator());
    }

    @AfterAll
    public static void tearDown() {
        executorService.shutdown();
    }


    @Test
    public void getOrCreateApplicationIdTest() {
        String testApplicationName = "test1";

        ApplicationUid before = applicationIdService.getApplicationUid(testServiceUid, testApplicationName);
        ApplicationUid applicationUid = applicationIdService.getOrCreateApplicationUid(testServiceUid, testApplicationName);

        Assertions.assertThat(before).isNull();
        Assertions.assertThat(applicationUid).isNotNull();
    }

    @Test
    public void getOrCreateApplicationIdConcurrentTest() throws InterruptedException, ExecutionException {
        String testApplicationName = "test2";
        int numberOfRequest = 3;

        ExecutorService localExecutorService = Executors.newFixedThreadPool(2);
        try {
            List<Callable<ApplicationUid>> tasks = new ArrayList<>();
            for (int i = 0; i < numberOfRequest; i++) {
                tasks.add(() -> applicationIdService.getOrCreateApplicationUid(testServiceUid, testApplicationName));
            }
            List<Future<ApplicationUid>> futures = localExecutorService.invokeAll(tasks);

            Set<ApplicationUid> result = new HashSet<>();
            for (Future<ApplicationUid> future : futures) {
                result.add(future.get());
            }

            Assertions.assertThat(result).hasSize(1);
        } finally {
            localExecutorService.shutdown();
        }
    }

}
