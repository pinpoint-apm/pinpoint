package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

public class ConcurrentMapApplicationUidDao implements ApplicationUidDao {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConcurrentMap<String, ApplicationUid> applicationUidMap = new ConcurrentHashMap<>();
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
    public ApplicationUid selectApplicationUid(ServiceUid serviceUid, String applicationName) {
        sleep(delay);
        return applicationUidMap.get(applicationName);
    }

    @Override
    public CompletableFuture<ApplicationUid> asyncSelectApplicationUid(ServiceUid serviceUid, String applicationName) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("select uid (name={})", applicationName);
            sleep(delay);
            return applicationUidMap.get(applicationName);
        }, executor);
    }

    @Override
    public boolean insertApplicationUidIfNotExists(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid) {
        logger.info("try insert uid (name={} -> {}", applicationName, applicationUid);
        ApplicationUid old = applicationUidMap.putIfAbsent(applicationName, applicationUid);
        return old == null;
    }

    @Override
    public CompletableFuture<Boolean> asyncInsertApplicationUidIfNotExists(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("try insert uid (name={} -> {}", applicationName, applicationUid);
            sleep(delay);
            return applicationUidMap.putIfAbsent(applicationName, applicationUid) == null;
        }, executor);
    }

    @Override
    public void deleteApplicationUid(ServiceUid serviceUid, String applicationName) {
        logger.info("delete uid (name={})", applicationName);
        sleep(delay);
        applicationUidMap.remove(applicationName);
    }

    @Override
    public List<String> selectApplicationNames(ServiceUid serviceUid) {
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
