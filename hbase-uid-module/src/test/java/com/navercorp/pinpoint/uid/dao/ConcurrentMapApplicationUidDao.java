package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

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
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        sleep(delay);
        return applicationUidMap.get(applicationUidAttribute);
    }

    @Override
    public CompletableFuture<ApplicationUid> asyncGetApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("select uid. {}", applicationUidAttribute);
            sleep(delay);
            return applicationUidMap.get(applicationUidAttribute);
        }, executor);
    }

    @Override
    public boolean putApplicationUidIfNotExists(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid) {
        logger.info("try insert uid. ({} -> {})", applicationUidAttribute, applicationUid);
        ApplicationUid old = applicationUidMap.putIfAbsent(applicationUidAttribute, applicationUid);
        return old == null;
    }

    @Override
    public CompletableFuture<Boolean> asyncPutApplicationUidIfNotExists(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid) {
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
    public List<ApplicationUidRow> scanApplicationUidRow(ServiceUid serviceUid) {
        throw new UnsupportedOperationException("scan not supported in ConcurrentMapApplicationUidDao");
    }

    @Override
    public List<ApplicationUidRow> scanApplicationUidRow(ServiceUid serviceUid, String applicationName) {
        throw new UnsupportedOperationException("scan not supported in ConcurrentMapApplicationUidDao");
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
