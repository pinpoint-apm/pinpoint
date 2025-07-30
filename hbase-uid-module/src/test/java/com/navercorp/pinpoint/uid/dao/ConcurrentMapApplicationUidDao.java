package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class ConcurrentMapApplicationUidDao implements ApplicationUidDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConcurrentMap<ApplicationUidAttribute, ApplicationUid> applicationUidMap = new ConcurrentHashMap<>();
    private final Executor executor;
    private final long delay;

    public ConcurrentMapApplicationUidDao(Executor executor, long delay) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.delay = delay;
    }

    public ConcurrentMapApplicationUidDao(Executor executor) {
        this(executor, 0);
    }

    @Override
    public ApplicationUid selectApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        sleep(delay);
        return applicationUidMap.get(applicationUidAttribute);
    }

    @Override
    public CompletableFuture<ApplicationUid> asyncSelectApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("select uid. {}", applicationUidAttribute);
            sleep(delay);
            return applicationUidMap.get(applicationUidAttribute);
        }, executor);
    }

    @Override
    public boolean insertApplicationUidIfNotExists(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid) {
        logger.info("try insert uid. ({} -> {})", applicationUidAttribute, applicationUid);
        ApplicationUid old = applicationUidMap.putIfAbsent(applicationUidAttribute, applicationUid);
        return old == null;
    }

    @Override
    public CompletableFuture<Boolean> asyncInsertApplicationUidIfNotExists(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("try insert uid. ({} -> {}", applicationUidAttribute, applicationUid);
            sleep(delay);
            return applicationUidMap.putIfAbsent(applicationUidAttribute, applicationUid) == null;
        }, executor);
    }

    @Override
    public void deleteApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        logger.info("delete uid. {}", applicationUidAttribute);
        sleep(delay);
        applicationUidMap.remove(applicationUidAttribute);
    }

    @Override
    public List<ApplicationUid> selectApplicationUid(ServiceUid serviceUid, String applicationName) {
        return applicationUidMap.entrySet().stream()
                .filter(entry -> entry.getKey().applicationName().equals(applicationName))
                .map(ConcurrentMap.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationUidAttribute> selectApplicationInfo(ServiceUid serviceUid) {
        return new ArrayList<>(applicationUidMap.keySet());
    }

    @Override
    public List<HbaseCellData> selectCellData(ServiceUid serviceUid) {
        throw new UnsupportedOperationException("not supported in ConcurrentMapApplicationUidDao");
    }

    private void sleep(long milliseconds) {
        if (milliseconds <= 0) {
            return;
        }

        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
