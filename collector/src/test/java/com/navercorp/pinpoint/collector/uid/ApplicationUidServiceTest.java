package com.navercorp.pinpoint.collector.uid;

import com.navercorp.pinpoint.collector.uid.dao.ApplicationNameDao;
import com.navercorp.pinpoint.collector.uid.dao.ApplicationUidDao;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidServiceImpl;
import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.RandomApplicationUidGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ApplicationUidServiceTest {

    private static final Logger logger = LogManager.getLogger(ApplicationUidServiceTest.class);

    private final ServiceUid testServiceUid = ServiceUid.DEFAULT_SERVICE_UID;

    private final ApplicationUidDao testApplicationIdDao = new ConcurrentMapApplicationUidDao();
    private final ApplicationNameDao testApplicationNameDao = new ConcurrentMapApplicationNameDao();
    private final ApplicationUidService applicationIdService = new ApplicationUidServiceImpl(testApplicationIdDao, testApplicationNameDao, new RandomApplicationUidGenerator());

    @Test
    public void getOrCreateApplicationIdTest() {
        String testApplicationName = "test1";

        ApplicationUid before = applicationIdService.getApplicationId(testServiceUid, testApplicationName);
        ApplicationUid applicationUid = applicationIdService.getOrCreateApplicationId(testServiceUid, testApplicationName);

        Assertions.assertThat(before).isNull();
        Assertions.assertThat(applicationUid).isNotNull();
    }

    @Test
    public void getOrCreateApplicationIdConcurrentTest() throws InterruptedException, ExecutionException {
        String testApplicationName = "test2";
        int numberOfConcurrency = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        List<Callable<ApplicationUid>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfConcurrency; i++) {
            tasks.add(() -> {
                return applicationIdService.getOrCreateApplicationId(testServiceUid, testApplicationName);
            });
        }

        List<Future<ApplicationUid>> futures = executorService.invokeAll(tasks);

        Set<ApplicationUid> result = new HashSet<>();
        for (Future<ApplicationUid> future : futures) {
            result.add(future.get());
        }

        Assertions.assertThat(result).hasSize(1);

        executorService.shutdown();
    }


    private static class ConcurrentMapApplicationUidDao implements ApplicationUidDao {

        private final ConcurrentMap<String, ApplicationUid> applicationUidMap = new ConcurrentHashMap<>();

        @Override
        public ApplicationUid selectApplicationUid(ServiceUid serviceUid, String applicationName) {
            return applicationUidMap.get(applicationName);
        }

        @Override
        public boolean insertApplicationUidIfNotExists(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid) {
            logger.info("try insert uid (name={} -> {}", applicationName, applicationUid);
            ApplicationUid old = applicationUidMap.putIfAbsent(applicationName, applicationUid);
            return old == null;
        }
    }

    private static class ConcurrentMapApplicationNameDao implements ApplicationNameDao {

        private final ConcurrentMap<ApplicationUid, String> applicationNameMap = new ConcurrentHashMap<>();

        @Override
        public boolean insertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName) {
            logger.info("try insert name ({} -> name={})", applicationUid, applicationName);
            return applicationNameMap.putIfAbsent(applicationUid, applicationName) == null;
        }

        @Override
        public void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
            logger.info("delete name ({} -> )", applicationUid);
            applicationNameMap.remove(applicationUid);
        }
    }

}
